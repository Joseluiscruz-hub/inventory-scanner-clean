# Cloud Functions — inventory authority

Node **20**. Callables (region `us-central1`):

| Name | Purpose |
|------|---------|
| `registerInventoryMovement` | Apply scan/adjust **deltas** to `inventarios`, write `movimientos_inventario` |
| `ejecutarTraspaso` | ADMIN transfer between almacenes in the same `planta` |

## Claims

| Claim | Values |
|-------|--------|
| `role` | `ADMIN` \| `OPERADOR` |
| `planta` | Plant id string (required for `OPERADOR`) |

Set via `scripts/firebase/set-user-role.mjs`.

## Deploy

```bash
cd functions && npm install && cd ..
firebase deploy --only functions,firestore:rules
```

## Client contract

Send **movement requests** (deltas + stable `operationId`), never a precomputed final stock.
Offline retries **must reuse** the same `operationId` (Room event UUID).
Stock document IDs are plant-stable: `planta__sku__almacen__tipoTarima`.
