/**
 * InventoryScannerPro — inventory authority Cloud Functions (Node 20).
 *
 * Callables:
 *   - registerInventoryMovement : validate auth/role/planta, apply stock delta, audit
 *   - ejecutarTraspaso          : server-side transfer between almacenes (same planta)
 *
 * Custom claims used:
 *   - role   : "ADMIN" | "OPERADOR"
 *   - planta : string (required for OPERADOR; ADMIN may cross plants via payload.planta)
 *
 * Document field for plant isolation: "planta"
 * Stock collection: inventarios (fields pallets, restos)
 * Audit: movimientos_inventario, traspasos_log
 *
 * Movement deltas may be signed (adjustments). Stock is floored at 0.
 * Clients must send quantity deltas + stable operationId (never final stock).
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

const ROLES = new Set(["ADMIN", "OPERADOR"]);
const INVENTARIOS = "inventarios";
const MOVIMIENTOS = "movimientos_inventario";
const TRASPASOS_LOG = "traspasos_log";

function requireAuth(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }
  return request.auth;
}

function requireRole(auth) {
  const role = auth.token && auth.token.role;
  if (!role || !ROLES.has(String(role).toUpperCase())) {
    throw new HttpsError(
      "permission-denied",
      "Missing or invalid claim 'role' (ADMIN|OPERADOR)."
    );
  }
  return String(role).toUpperCase();
}

function resolvePlanta(auth, role, requestedPlanta) {
  const claimPlanta =
    auth.token && typeof auth.token.planta === "string"
      ? auth.token.planta.trim()
      : "";
  if (role === "ADMIN") {
    const planta = (requestedPlanta || claimPlanta || "").trim();
    if (!planta) {
      throw new HttpsError(
        "failed-precondition",
        "ADMIN must send payload.planta or have claim planta."
      );
    }
    return planta;
  }
  if (!claimPlanta) {
    throw new HttpsError(
      "failed-precondition",
      "OPERADOR requires custom claim 'planta'."
    );
  }
  if (requestedPlanta && String(requestedPlanta).trim() !== claimPlanta) {
    throw new HttpsError(
      "permission-denied",
      "OPERADOR cannot act on a different planta than claim."
    );
  }
  return claimPlanta;
}

function asNonNegInt(value, field) {
  const n = Number(value);
  if (!Number.isFinite(n) || !Number.isInteger(n) || n < 0) {
    throw new HttpsError("invalid-argument", `${field} must be an integer >= 0.`);
  }
  return n;
}

function asSignedInt(value, field) {
  const n = Number(value);
  if (!Number.isFinite(n) || !Number.isInteger(n)) {
    throw new HttpsError("invalid-argument", `${field} must be an integer.`);
  }
  return n;
}

/** Allow signed deltas (adjustments / corrections). Sum of abs must be > 0. */
function asMovementDeltas(palletsDelta, restosDelta) {
  const p = asSignedInt(palletsDelta, "palletsDelta");
  const r = asSignedInt(restosDelta, "restosDelta");
  if (Math.abs(p) + Math.abs(r) <= 0) {
    throw new HttpsError(
      "invalid-argument",
      "At least one of palletsDelta/restosDelta must be non-zero."
    );
  }
  return { palletsDelta: p, restosDelta: r };
}

function stockDocId(planta, sku, almacen, tipoTarima) {
  return [planta, sku, almacen, tipoTarima]
    .map((s) => String(s).replace(/[\/\s:]+/g, "_"))
    .join("__");
}

/**
 * registerInventoryMovement
 * Client sends a *movement request* (deltas), not final stock.
 *
 * data: {
 *   sku, almacen, tipoTarima,
 *   palletsDelta, restosDelta,   // signed integers, abs sum > 0
 *   deviceId, operationId,       // operationId for idempotency (reuse on retry)
 *   descripcion?, planta?        // planta only needed for ADMIN without claim
 * }
 */
exports.registerInventoryMovement = onCall({ region: "us-central1" }, async (request) => {
  const auth = requireAuth(request);
  const role = requireRole(auth);
  const data = request.data || {};

  const sku = String(data.sku || "").trim();
  const almacen = String(data.almacen || "").trim();
  const tipoTarima = String(data.tipoTarima || "N/A").trim() || "N/A";
  const deviceId = String(data.deviceId || "").trim();
  const operationId = String(data.operationId || "").trim();
  const descripcion = String(data.descripcion || "").trim();

  if (!sku || !almacen || !deviceId || !operationId) {
    throw new HttpsError(
      "invalid-argument",
      "sku, almacen, deviceId and operationId are required."
    );
  }

  const planta = resolvePlanta(auth, role, data.planta);
  const { palletsDelta, restosDelta } = asMovementDeltas(
    data.palletsDelta,
    data.restosDelta
  );

  const auditRef = db.collection(MOVIMIENTOS).doc(operationId);
  const invRef = db.collection(INVENTARIOS).doc(
    stockDocId(planta, sku, almacen, tipoTarima)
  );

  const result = await db.runTransaction(async (tx) => {
    const existingAudit = await tx.get(auditRef);
    if (existingAudit.exists) {
      return {
        idempotent: true,
        ...(existingAudit.data().result || {}),
      };
    }

    const invSnap = await tx.get(invRef);
    const prevPallets = invSnap.exists ? Number(invSnap.get("pallets") || 0) : 0;
    const prevRestos = invSnap.exists ? Number(invSnap.get("restos") || 0) : 0;
    // Floor at 0 so negative adjustments never produce negative stock.
    const nextPallets = Math.max(0, prevPallets + palletsDelta);
    const nextRestos = Math.max(0, prevRestos + restosDelta);

    const stockPayload = {
      sku,
      descripcion: descripcion || (invSnap.exists ? invSnap.get("descripcion") || "" : ""),
      almacen,
      tipoTarima,
      pallets: nextPallets,
      restos: nextRestos,
      planta,
      authUid: auth.uid,
      deviceId,
      updatedAt: FieldValue.serverTimestamp(),
      updatedByRole: role,
    };
    if (!invSnap.exists) {
      stockPayload.createdAt = FieldValue.serverTimestamp();
    }
    tx.set(invRef, stockPayload, { merge: true });

    const movementResult = {
      inventariosDocId: invRef.id,
      planta,
      sku,
      almacen,
      tipoTarima,
      palletsDelta,
      restosDelta,
      palletsAfter: nextPallets,
      restosAfter: nextRestos,
    };

    tx.set(auditRef, {
      operationId,
      type: "SCAN_OR_ADJUST",
      planta,
      sku,
      almacen,
      tipoTarima,
      palletsDelta,
      restosDelta,
      deviceId,
      authUid: auth.uid,
      role,
      descripcion,
      result: movementResult,
      createdAt: FieldValue.serverTimestamp(),
    });

    return { idempotent: false, ...movementResult };
  });

  return result;
});

/**
 * ejecutarTraspaso
 * Server-side transfer between almacenes within the same planta.
 *
 * data: {
 *   sku, tipoTarima,
 *   almacenOrigen, almacenDestino,
 *   palletsMover, restosMover,   // integers >= 0, sum > 0
 *   deviceId, operationId,
 *   planta?                      // ADMIN without claim
 * }
 */
exports.ejecutarTraspaso = onCall({ region: "us-central1" }, async (request) => {
  const auth = requireAuth(request);
  const role = requireRole(auth);
  const data = request.data || {};

  // Transfers are ADMIN-only (matches previous client rules for traspasos_log).
  if (role !== "ADMIN") {
    throw new HttpsError("permission-denied", "ejecutarTraspaso requires role ADMIN.");
  }

  const sku = String(data.sku || "").trim();
  const tipoTarima = String(data.tipoTarima || "N/A").trim() || "N/A";
  const almacenOrigen = String(data.almacenOrigen || "").trim();
  const almacenDestino = String(data.almacenDestino || "").trim();
  const deviceId = String(data.deviceId || "").trim();
  const operationId = String(data.operationId || "").trim();

  if (!sku || !almacenOrigen || !almacenDestino || !deviceId || !operationId) {
    throw new HttpsError(
      "invalid-argument",
      "sku, almacenOrigen, almacenDestino, deviceId and operationId are required."
    );
  }
  if (almacenOrigen === almacenDestino) {
    throw new HttpsError("invalid-argument", "Origin and destination must differ.");
  }

  const planta = resolvePlanta(auth, role, data.planta);
  const palletsMover = asNonNegInt(data.palletsMover, "palletsMover");
  const restosMover = asNonNegInt(data.restosMover, "restosMover");
  if (palletsMover + restosMover <= 0) {
    throw new HttpsError(
      "invalid-argument",
      "At least one of palletsMover/restosMover must be > 0."
    );
  }

  const logRef = db.collection(TRASPASOS_LOG).doc(operationId);
  const origenRef = db.collection(INVENTARIOS).doc(
    stockDocId(planta, sku, almacenOrigen, tipoTarima)
  );
  const destinoRef = db.collection(INVENTARIOS).doc(
    stockDocId(planta, sku, almacenDestino, tipoTarima)
  );

  const result = await db.runTransaction(async (tx) => {
    const existingLog = await tx.get(logRef);
    if (existingLog.exists) {
      return { idempotent: true, ...(existingLog.data().result || {}) };
    }

    const origenSnap = await tx.get(origenRef);
    const destinoSnap = await tx.get(destinoRef);

    if (!origenSnap.exists) {
      throw new HttpsError(
        "not-found",
        `No stock for SKU ${sku} in ${almacenOrigen} (planta ${planta}).`
      );
    }

    const origenPallets = Number(origenSnap.get("pallets") || 0);
    const origenRestos = Number(origenSnap.get("restos") || 0);
    if (palletsMover > origenPallets || restosMover > origenRestos) {
      throw new HttpsError(
        "failed-precondition",
        `Insufficient stock. Available pallets=${origenPallets} restos=${origenRestos}.`
      );
    }

    const newOrigenPallets = origenPallets - palletsMover;
    const newOrigenRestos = origenRestos - restosMover;
    const destPallets = destinoSnap.exists ? Number(destinoSnap.get("pallets") || 0) : 0;
    const destRestos = destinoSnap.exists ? Number(destinoSnap.get("restos") || 0) : 0;
    const descripcion = origenSnap.get("descripcion") || "";

    if (newOrigenPallets === 0 && newOrigenRestos === 0) {
      tx.delete(origenRef);
    } else {
      tx.update(origenRef, {
        pallets: newOrigenPallets,
        restos: newOrigenRestos,
        updatedAt: FieldValue.serverTimestamp(),
        updatedByRole: role,
        authUid: auth.uid,
        deviceId,
      });
    }

    tx.set(
      destinoRef,
      {
        sku,
        descripcion,
        almacen: almacenDestino,
        tipoTarima,
        pallets: destPallets + palletsMover,
        restos: destRestos + restosMover,
        planta,
        authUid: auth.uid,
        deviceId,
        origenTraspaso: almacenOrigen,
        updatedAt: FieldValue.serverTimestamp(),
        updatedByRole: role,
        createdAt: destinoSnap.exists
          ? destinoSnap.get("createdAt") || FieldValue.serverTimestamp()
          : FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    const transferResult = {
      planta,
      sku,
      tipoTarima,
      almacenOrigen,
      almacenDestino,
      palletsMover,
      restosMover,
      origenDocId: origenRef.id,
      destinoDocId: destinoRef.id,
    };

    tx.set(logRef, {
      operationId,
      sku,
      descripcion,
      planta,
      almacenOrigen,
      almacenDestino,
      pallets: palletsMover,
      restos: restosMover,
      tipoTarima,
      authUid: auth.uid,
      deviceId,
      role,
      estado: "COMPLETADO",
      result: transferResult,
      createdAt: FieldValue.serverTimestamp(),
    });

    return { idempotent: false, ...transferResult };
  });

  return result;
});
