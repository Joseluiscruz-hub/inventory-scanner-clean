import admin from "firebase-admin";

const args = new Set(process.argv.slice(2));
const shouldDelete = args.has("--delete");
const collectionName = "inventario_referencia";
const skuPattern = /^[A-Z0-9][A-Z0-9._/-]{0,63}$/;
const corruptMarkers = [
  "PK\u0003\u0004",
  "[CONTENT_TYPES]",
  "XL/",
  "XL_",
  "PIVOTCACHE",
  ".XML",
  ".EMF"
];

if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error("Falta GOOGLE_APPLICATION_CREDENTIALS apuntando al JSON de service account.");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

function looksCorrupt(value) {
  if (typeof value !== "string") return false;
  const upper = value.toUpperCase();
  return /[\u0000-\u0008\u000B\u000C\u000E-\u001F]/.test(value) ||
    corruptMarkers.some((marker) => upper.includes(marker));
}

function isValidSku(value) {
  if (typeof value !== "string") return false;
  const sku = value.trim().toUpperCase();
  return sku.length > 0 &&
    sku.length <= 64 &&
    skuPattern.test(sku) &&
    !looksCorrupt(sku);
}

function isCorruptReference(data) {
  return !isValidSku(data.sku) ||
    looksCorrupt(data.descripcion) ||
    looksCorrupt(data.armado) ||
    looksCorrupt(data.tarimaPlasticaSku);
}

const firestore = admin.firestore();
const snapshot = await firestore.collection(collectionName).get();
const corruptDocs = snapshot.docs.filter((doc) => isCorruptReference(doc.data()));

console.log(`Documentos revisados: ${snapshot.size}`);
console.log(`Documentos corruptos detectados: ${corruptDocs.length}`);

corruptDocs.slice(0, 20).forEach((doc) => {
  const sku = String(doc.get("sku") ?? doc.id);
  console.log(`- ${doc.id}: ${sku.slice(0, 80)}`);
});

if (!shouldDelete) {
  console.log("Dry-run: no se borro nada. Ejecuta con --delete para eliminar.");
  process.exit(0);
}

let batch = firestore.batch();
let pending = 0;
let deleted = 0;

for (const doc of corruptDocs) {
  batch.delete(doc.ref);
  pending++;
  deleted++;

  if (pending === 450) {
    await batch.commit();
    batch = firestore.batch();
    pending = 0;
  }
}

if (pending > 0) {
  await batch.commit();
}

console.log(`Documentos corruptos eliminados: ${deleted}`);
