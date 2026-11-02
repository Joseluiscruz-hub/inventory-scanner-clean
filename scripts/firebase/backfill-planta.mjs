/**
 * Backfill missing `planta` on inventarios / inventario_referencia /
 * traspasos_log / movimientos_inventario.
 *
 * Usage:
 *   cd scripts/firebase && npm install
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
 *   npm run backfill-planta -- --planta=Norte              # dry-run (default)
 *   npm run backfill-planta -- --planta=Norte --apply      # write
 *   npm run backfill-planta -- --planta=Norte --collections=inventarios,inventario_referencia
 *
 * Signed-off-by: José Luis Cruz Prieto <141293124+Joseluiscruz-hub@users.noreply.github.com>
 */
import admin from "firebase-admin";

const DEFAULT_COLLECTIONS = [
  "inventarios",
  "inventario_referencia",
  "traspasos_log",
  "movimientos_inventario"
];

function parseArgs(argv) {
  const out = {
    planta: null,
    apply: false,
    collections: DEFAULT_COLLECTIONS.slice()
  };
  for (const arg of argv) {
    if (arg === "--apply") out.apply = true;
    else if (arg.startsWith("--planta=")) out.planta = arg.slice("--planta=".length).trim();
    else if (arg.startsWith("--collections=")) {
      out.collections = arg
        .slice("--collections=".length)
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean);
    } else if (arg === "--help" || arg === "-h") {
      out.help = true;
    }
  }
  return out;
}

const args = parseArgs(process.argv.slice(2));
if (args.help || !args.planta) {
  console.error(
    "Uso: npm run backfill-planta -- --planta=<id> [--apply] [--collections=a,b]"
  );
  console.error("Sin --apply solo imprime el dry-run.");
  process.exit(args.help ? 0 : 1);
}

if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error(
    "Falta GOOGLE_APPLICATION_CREDENTIALS apuntando al JSON de service account."
  );
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

const db = admin.firestore();
const planta = args.planta;

console.log(`planta destino: ${planta}`);
console.log(`modo: ${args.apply ? "APPLY (escribe)" : "DRY-RUN"}`);
console.log(`colecciones: ${args.collections.join(", ")}`);

let totalMissing = 0;
let totalUpdated = 0;

for (const collectionName of args.collections) {
  const snap = await db.collection(collectionName).get();
  const missing = snap.docs.filter((doc) => {
    const value = doc.get("planta");
    return typeof value !== "string" || value.trim().length === 0;
  });
  console.log(
    `\n[${collectionName}] docs=${snap.size} sin_planta=${missing.length}`
  );
  missing.slice(0, 15).forEach((doc) => {
    console.log(`  - ${doc.id}`);
  });
  if (missing.length > 15) {
    console.log(`  ... y ${missing.length - 15} más`);
  }
  totalMissing += missing.length;

  if (!args.apply || missing.length === 0) continue;

  let batch = db.batch();
  let pending = 0;
  for (const doc of missing) {
    batch.update(doc.ref, {
      planta,
      plantaBackfilledAt: admin.firestore.FieldValue.serverTimestamp(),
      plantaBackfillSource: "scripts/firebase/backfill-planta.mjs"
    });
    pending++;
    totalUpdated++;
    if (pending === 450) {
      await batch.commit();
      batch = db.batch();
      pending = 0;
    }
  }
  if (pending > 0) await batch.commit();
  console.log(`[${collectionName}] actualizados=${missing.length}`);
}

console.log(`\nResumen: missing=${totalMissing} updated=${totalUpdated}`);
if (!args.apply) {
  console.log("Dry-run completo. Re-ejecuta con --apply para escribir.");
}
