<!-- paste into README Seguridad section -->
### Autoridad de inventario (Cloud Functions)

- `registerInventoryMovement` — escaneo / ajuste (deltas + `operationId`)
- `ejecutarTraspaso` — traspaso ADMIN

Claim: **`planta`**. Deploy: `firebase deploy --only functions,firestore:rules`.
Firma release: `keystore.properties.example`.
