/**
 * Copy/merge legacy device-scoped inventarios docs into plant-stable IDs
 * used by Cloud Functions (planta__sku__almacen__tipoTarima).
 *
 * Strategy (non-destructive):
 *   1. For each inventarios doc, resolve planta (field or --planta default).
 *   2. Compute plant-stable id; if different from current id, merge stock
 *      into the plant-stable doc (sum pallets/restos) and stamp the legacy
 *      doc with legacyMigratedAt + legacyStatus=COPIED_TO_PLANT_STABLE.
 *   3. Never deletes unless --delete-legacy is passed AFTER a successful copy.
 *
 * Usage:
 *   npm run migrate-inventarios-ids -- --planta=Norte           # dry-run
 *   npm run migrate-inventarios-ids -- --planta=Norte --apply
 *   npm run migrate-inventarios-ids -- --planta=Norte --apply --delete-legacy
 *
 * Signed-off-by: José Luis Cruz Prieto <141293124+Joseluiscruz-hub@users.noreply.github.com>
 */
import admin from "firebase-admin";

function parseArgs(argv) {
  const out = { planta: null, apply: false, deleteLegacy: false };
  for (const arg of argv) {
    if (arg === "--apply") out.apply = true;
    else if (arg === "--delete-legacy") out.deleteLegacy = true;
    else if (arg.startsWith("--planta=")) out.planta = arg.slice("--planta=".length).trim();
    else if (arg === "--help" || arg === "-h") out.help = true;
  }
  return out;
}

function stockDocId(planta, sku, almacen, tipoTarima) {
  return [planta, sku, almacen, tipoTarima]
    .map((s) => String(s).replace(/[\/\s:]+/g, "_"))
    .join("__");
}

const args = parseArgs(process.argv.slice(2));
if (args.help || !args.planta) {
  console.error(
    "Uso: npm run migrate-inventarios-ids -- --planta=<id> [--apply] [--delete-legacy]"
  );
  process.exit(args.help ? 0 : 1);
}
if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error("Falta GOOGLE_APPLICATION_CREDENTIALS.");
  process.exit(1);
}

admin.initializeApp({ credential: admin.credential.applicationDefault() });
const db = admin.firestore();
const defaultPlanta = args.planta;

console.log(`default planta: ${defaultPlanta}`);
console.log(`modo: ${args.apply ? "APPLY" : "DRY-RUN"}`);
console.log(`delete-legacy: ${args.deleteLegacy}`);

const snap = await db.collection("inventarios").get();
let alreadyStable = 0;
let toMigrate = 0;
let migrated = 0;
let deleted = 0;

for (const doc of snap.docs) {
  const data = doc.data() || {};
  if (data.legacyStatus === "COPIED_TO_PLANT_STABLE") {
    continue;
  }
  const sku = String(data.sku || "").trim();
  const almacen = String(data.almacen || "").trim();
  const tipoTarima = String(data.tipoTarima || "N/A").trim() || "N/A";
  const planta =
    typeof data.planta === "string" && data.planta.trim()
      ? data.planta.trim()
      : defaultPlanta;
  if (!sku || !almacen) {
    console.log(`skip (sin sku/almacen): ${doc.id}`);
    continue;
  }
  const stableId = stockDocId(planta, sku, almacen, tipoTarima);
  if (stableId === doc.id) {
    alreadyStable++;
    // Ensure planta field present on stable docs
    if (args.apply && (!data.planta || data.planta !== planta)) {
      await doc.ref.set(
        {
          planta,
          plantaBackfilledAt: admin.firestore.FieldValue.serverTimestamp()
        },
        { merge: true }
      );
    }
    continue;
  }

  toMigrate++;
  const pallets = Number(data.pallets || 0) || 0;
  const restos = Number(data.restos || 0) || 0;
  console.log(
    `migrate ${doc.id} → ${stableId} (pallets=${pallets} restos=${restos} planta=${planta})`
  );

  if (!args.apply) continue;

  const stableRef = db.collection("inventarios").doc(stableId);
  await db.runTransaction(async (tx) => {
    const stableSnap = await tx.get(stableRef);
    const prevP = stableSnap.exists ? Number(stableSnap.get("pallets") || 0) : 0;
    const prevR = stableSnap.exists ? Number(stableSnap.get("restos") || 0) : 0;
    const payload = {
      sku,
      descripcion: data.descripcion || (stableSnap.exists ? stableSnap.get("descripcion") || "" : ""),
      almacen,
      tipoTarima,
      pallets: prevP + pallets,
      restos: prevR + restos,
      planta,
      migratedFrom: doc.id,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedByRole: "MIGRATION_SCRIPT"
    };
    if (!stableSnap.exists) {
      payload.createdAt = admin.firestore.FieldValue.serverTimestamp();
    }
    tx.set(stableRef, payload, { merge: true });
    tx.set(
      doc.ref,
      {
        planta,
        legacyStatus: "COPIED_TO_PLANT_STABLE",
        legacyMigratedAt: admin.firestore.FieldValue.serverTimestamp(),
        legacyTargetId: stableId
      },
      { merge: true }
    );
  });
  migrated++;

  if (args.deleteLegacy) {
    await doc.ref.delete();
    deleted++;
  }
}

console.log(
  `\nResumen: total=${snap.size} alreadyStable=${alreadyStable} toMigrate=${toMigrate} migrated=${migrated} deleted=${deleted}`
);
if (!args.apply) {
  console.log("Dry-run completo. Re-ejecuta con --apply para copiar/merge.");
}
