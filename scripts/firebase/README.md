# Firebase admin scripts — InventoryScannerPro

Requiere Node 20+ y un service account con permisos Auth + Firestore.

```bash
cd scripts/firebase && npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
```

## Roles / claims

```bash
npm run set-role -- operador@example.com OPERADOR Norte
npm run set-role -- admin@example.com ADMIN
```

## Backfill `planta` (REQUIRED post-rules)

Legacy docs without `planta` are ADMIN-read-only under current rules.

```bash
# Dry-run
npm run backfill-planta -- --planta=Norte

# Apply
npm run backfill-planta -- --planta=Norte --apply
```

## Migrate inventarios → plant-stable IDs

Cloud Functions write stock under `planta__sku__almacen__tipoTarima`.
Legacy device-scoped ids (`..._deviceId`) should be copied/merged first.

```bash
npm run migrate-inventarios-ids -- --planta=Norte
npm run migrate-inventarios-ids -- --planta=Norte --apply
# Optional cleanup after verifying plant-stable docs:
npm run migrate-inventarios-ids -- --planta=Norte --apply --delete-legacy
```

## Clean corrupt referencia

```bash
npm run clean-reference
npm run clean-reference -- --delete
```

## operationId contract (client ↔ Functions)

- Every local scan/adjust event has a stable UUID `operationId` (Room PK).
- Offline queue retries **must reuse** the same `operationId`.
- Server stores audit under `movimientos_inventario/{operationId}` / `traspasos_log/{operationId}` and returns `idempotent: true` on replay.
- Payload is always **deltas** (`palletsDelta` / `restosDelta`), never a precomputed final stock.
