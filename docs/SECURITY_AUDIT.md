# Security audit — InventoryScannerPro

Fecha: 2026-09-20 (America/Mexico_City)  
Alcance: `master` + remediaciones en `harden/authority-functions-plant` + follow-up `harden/planta-backfill-offline-deltas`

> **Estado post-PR:** C1/H1 (Functions), C2 (claim `planta`), H3 (signingConfigs) y M1
> (merge CSV) están abordados. Detalle: [`SECURITY_AUDIT_UPDATE.md`](SECURITY_AUDIT_UPDATE.md).

## Resumen ejecutivo

| Severidad | Finding | Estado |
|-----------|---------|--------|
| Critical | C1 Cliente escribe stock final | **Remediado** — `registerInventoryMovement` / `ejecutarTraspaso`; rules niegan writes de stock |
| Critical/High | C2 Sin aislamiento por planta | **Remediado** — claim + campo `planta` |
| High | H1 Cloud Functions ausentes | **Remediado** — `functions/` + `firebase.json` |
| High | H3 Firma release no cableada | **Remediado** — `keystore.properties` / `ISP_*` |
| Medium | M1 Sync sobrescribe CSV | **Mitigado** — merge + `.bak` |
| Medium | M2 Tests de rules en CI | **Mitigado (best-effort)** — `tests/firestore-rules` + workflow |
| Medium | M3 Permisos amplios residuales | Abierto |

## Claim y deploy

- Claim de planta: **`planta`**
- Set claims: `scripts/firebase/set-user-role.mjs`
- Deploy: `firebase deploy --only functions,firestore:rules`

## Post-deploy REQUIRED scripts

```bash
cd scripts/firebase && npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json

# 1) Backfill missing planta (dry-run then apply)
npm run backfill-planta -- --planta=Norte
npm run backfill-planta -- --planta=Norte --apply

# 2) Migrate device-scoped inventarios IDs → plant-stable
npm run migrate-inventarios-ids -- --planta=Norte
npm run migrate-inventarios-ids -- --planta=Norte --apply
```

## operationId + offline queue

- Local Room events use UUID `operationId` (PK).
- `OfflineInventorySyncScheduler` / `SincronizarColaOfflineUseCase` drain PENDING/FAILED
  via `registerInventoryMovement` with **deltas** and the **same** `operationId` on retries.
- Server audit doc id = `operationId` → idempotent replay returns `idempotent: true`.
