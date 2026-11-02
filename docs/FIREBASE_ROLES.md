# Configuración de Firebase y roles

| Claim | Valores | Notas |
|-------|---------|-------|
| `role` | `ADMIN` \| `OPERADOR` | Obligatorio |
| `planta` | string | Obligatorio para `OPERADOR` |

Campo documento: **`planta`** en `inventarios`, `inventario_referencia`, `traspasos_log`, `movimientos_inventario`.

```bash
cd scripts/firebase && npm install
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-service-account.json
npm run set-role -- operador@example.com OPERADOR Norte
npm run set-role -- admin@example.com ADMIN
```

| Callable | Quién |
|----------|-------|
| `registerInventoryMovement` | OPERADOR / ADMIN — deltas + `operationId` |
| `ejecutarTraspaso` | ADMIN |

```bash
cd functions && npm install && cd ..
firebase deploy --only functions,firestore:rules

# Then backfill planta + migrate inventarios IDs (see scripts/firebase/README.md)
npm --prefix scripts/firebase run backfill-planta -- --planta=Norte --apply
npm --prefix scripts/firebase run migrate-inventarios-ids -- --planta=Norte --apply
```
