# Seguridad y despliegue

- Claim `role` (`ADMIN` / `OPERADOR`) + aislamiento por **`planta`**.
- Stock final lo escriben Cloud Functions (`registerInventoryMovement`, `ejecutarTraspaso`).
- Deny-by-default en `firestore.rules`.
- Firma release: `keystore.properties` / env `ISP_*` (ver `keystore.properties.example`).

Gaps: backfill `planta`, rules tests en CI, cola offline con deltas + `operationId`.

```bash
firebase deploy --only functions,firestore:rules
```
