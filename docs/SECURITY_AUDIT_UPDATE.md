# Security audit update — authority Functions + plant isolation + offline deltas

Date: 2026-09-20 (America/Mexico_City)

## Closed / mitigated

| ID | Change |
|----|--------|
| C1 / H1 | Stock writes moved to Cloud Functions `registerInventoryMovement` and `ejecutarTraspaso`. Clients no longer write final `pallets`/`restos`. |
| C2 | Firestore rules isolate by custom claim **`planta`** and document field **`planta`**. ADMIN may cross plants; OPERADOR must match. |
| H3 | Release `signingConfigs` read from `keystore.properties` / `ISP_*` env. Example only in git (`keystore.properties.example`). |
| M1 | Cloud sync use case merges by SKU+almacen+tipoTarima (cloud wins) + `.bak` instead of blind overwrite. |
| Offline queue | Client drains Room PENDING/FAILED with deltas + stable `operationId` (retries reuse UUID). |
| Referencia | Client stamps/filters `planta` on `inventario_referencia` reads/writes. |
| Plant-stable IDs | New Function writes already use `planta__sku__almacen__tipoTarima`; admin script migrates legacy device-scoped docs (copy+mark). |
| M2 | Minimal `@firebase/rules-unit-testing` suite + GitHub Action `firestore-rules.yml`. |

## Claim name

- **`planta`** (string)
- **`role`**: `ADMIN` | `OPERADOR`

```bash
cd scripts/firebase && npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
npm run set-role -- operador@example.com OPERADOR Norte
npm run set-role -- admin@example.com ADMIN
```

## Deploy

```bash
cd functions && npm install && cd ..
firebase deploy --only functions,firestore:rules
```

## Backfill + migration (REQUIRED after rules deploy)

```bash
cd scripts/firebase && npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
npm run backfill-planta -- --planta=Norte              # dry-run
npm run backfill-planta -- --planta=Norte --apply
npm run migrate-inventarios-ids -- --planta=Norte      # dry-run
npm run migrate-inventarios-ids -- --planta=Norte --apply
```

See `scripts/firebase/README.md`.

## Remaining gaps

- Negative stock adjustments still rely on signed deltas floored at 0 server-side; conflict UX for multi-device races is best-effort.
- Optional `--delete-legacy` after plant-stable migration is manual/ops-owned.
- Rules CI requires Firebase emulator availability on the runner.
