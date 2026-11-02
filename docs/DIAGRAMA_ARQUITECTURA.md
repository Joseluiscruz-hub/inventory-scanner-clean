# 🏗️ Diagrama de Arquitectura Reactiva - EscaneoDemo

## Vista General del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE PRESENTACIÓN (UI)                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────┐           ┌──────────────────────┐            │
│  │ InventarioCompose    │◄─────────►│ ComparacionTiempo    │            │
│  │ Activity             │  Shared   │ Real Activity        │            │
│  │ (Captura de Datos)   │  ViewModel │ (Visualización)      │            │
│  └──────────┬───────────┘           └──────────┬───────────┘            │
│             │                                  │                         │
│             │  activityViewModels()            │  activityViewModels()   │
│             └──────────────┬───────────────────┘                         │
│                            │                                             │
│                            ▼                                             │
│              ┌─────────────────────────────┐                             │
│              │  InventarioSharedViewModel  │                             │
│              │                             │                             │
│              │  • comparacion              │                             │
│              │  • ultimoItemEscaneado      │                             │
│              │  • estadisticas             │                             │
│              │  • diferenciasCriticas      │                             │
│              └──────────────┬──────────────┘                             │
└─────────────────────────────┼─────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE DOMINIO                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│              ┌─────────────────────────────┐                            │
│              │ InventarioTiempoRealManager │                            │
│              │                             │                            │
│              │  • _inventarioReferencia    │                            │
│              │    (HashMap<String, Item>)  │                            │
│              │                             │                            │
│              │  • cargarInventarioSistema()│                            │
│              │  • comparacionEnTiempoReal  │◄─── Flow Combine           │
│              │  • ultimoItemEscaneado      │                            │
│              └──────────────┬──────────────┘                            │
│                             │                                           │
│                             │ combine()                                 │
│                             ▼                                           │
│              ┌─────────────────────────────┐                            │
│              │     Flow Combine Logic      │                            │
│              │                             │                            │
│              │  referencia + escaneados →  │                            │
│              │  List<DiferenciaInventario> │                            │
│              └──────────────┬──────────────┘                            │
└─────────────────────────────┼─────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE DATOS                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│              ┌─────────────────────────────┐                            │
│              │  InventarioRepository       │                            │
│              │  (Única Fuente de Verdad)   │                            │
│              │                             │                            │
│              │  • observarEscaneados()     │◄─── Flow                   │
│              │  • registrarScan()          │◄─── suspend                │
│              │  • limpiarInventario()      │◄─── suspend                │
│              └──────────────┬──────────────┘                            │
│                             │                                           │
│                             ▼                                           │
│              ┌─────────────────────────────┐                            │
│              │      InventarioDao          │                            │
│              │      (Room DAO)             │                            │
│              │                             │                            │
│              │  @Query Flow<List<..>>      │◄─── Room notifica          │
│              │  @Insert suspend            │     automáticamente        │
│              │  @Update suspend            │     en INSERT/UPDATE/DELETE│
│              │  @Delete suspend            │                            │
│              └──────────────┬──────────────┘                            │
│                             │                                           │
│                             ▼                                           │
│              ┌─────────────────────────────┐                            │
│              │   Room Database (SQLite)    │                            │
│              │                             │                            │
│              │  TABLE: inventario_escaneado│                            │
│              │  ┌───────────────────────┐  │                            │
│              │  │ codigo (PK) TEXT      │  │                            │
│              │  │ descripcion TEXT      │  │                            │
│              │  │ cantidadTotal INT     │  │                            │
│              │  │ ultimoScanTimestamp   │  │                            │
│              │  └───────────────────────┘  │                            │
│              └─────────────────────────────┘                            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Flujo de Datos: Escaneo → UI

```
┌──────────────────────────────────────────────────────────────────────────┐
│  SECUENCIA: Usuario escanea código → UI actualizada en <100ms          │
└──────────────────────────────────────────────────────────────────────────┘

1. USUARIO ESCANEA
   ┌────────────────────┐
   │ Usuario apunta     │
   │ cámara a código    │
   │ de barras          │
   └─────────┬──────────┘
             │
             ▼
2. INVENTARIO COMPOSE ACTIVITY PROCESA
   ┌─────────────────────────────────────────┐
   │ fun procesarResultadoEscaneo(codigo) {  │
   │   // Lógica existente                   │
   │                                         │
   │   // ✅ NUEVO: Registrar en Room        │
   │   sharedViewModel.registrarScan(        │
   │     codigo = material.sku,              │
   │     descripcion = material.descripcion, │
   │     cantidad = 1                        │
   │   )                                     │
   │ }                                       │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
3. SHAREDVIEWMODEL LLAMA AL REPOSITORIO
   ┌─────────────────────────────────────────┐
   │ fun registrarScan(codigo, desc, cant) { │
   │   viewModelScope.launch {               │
   │     repository.registrarScan(...)       │
   │   }                                     │
   │ }                                       │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
4. REPOSITORY INSERTA EN ROOM
   ┌─────────────────────────────────────────┐
   │ suspend fun registrarScan(...) {        │
   │   val existing = dao.obtenerItem(...)   │
   │   val nueva = InventarioEscaneadoEntity(│
   │     codigo = codigo,                    │
   │     descripcion = descripcion,          │
   │     cantidadTotal = existing + cantidad,│
   │     ultimoScanTimestamp = now           │
   │   )                                     │
   │   dao.insertarOActualizar(nueva)        │
   │   // ✅ Room notifica a los observers   │
   │ }                                       │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
5. ROOM EJECUTA INSERT
   ┌─────────────────────────────────────────┐
   │ INSERT INTO inventario_escaneado        │
   │ (codigo, descripcion, cantidadTotal,    │
   │  ultimoScanTimestamp)                   │
   │ VALUES (?, ?, ?, ?)                     │
   │                                         │
   │ ✅ Room INVALIDA los Flows observando   │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
6. FLOW DEL DAO EMITE NUEVA LISTA
   ┌─────────────────────────────────────────┐
   │ @Query("SELECT * FROM ... ORDER BY ...")│
   │ fun observarInventarioEscaneado():      │
   │   Flow<List<InventarioEscaneadoEntity>> │
   │                                         │
   │ Emite: [Item1, Item2, Item3, ...]       │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
7. MANAGER COMBINA CON REFERENCIA
   ┌─────────────────────────────────────────┐
   │ val comparacionEnTiempoReal = combine(  │
   │   _inventarioReferencia,                │
   │   repository.observarEscaneados()       │
   │ ) { referencia, escaneados ->           │
   │                                         │
   │   // Comparar cada escaneado vs ref    │
   │   escaneados.forEach { escaneado ->     │
   │     val ref = referencia[escaneado.codigo]│
   │     val diff = escaneado.cantidad - ref.cantidad│
   │                                         │
   │     diferencias.add(DiferenciaInventario(│
   │       codigo = escaneado.codigo,        │
   │       cantidadReferencia = ref.cantidad,│
   │       cantidadActual = escaneado.cantidad,│
   │       diferencia = diff,                │
   │       estado = when {                   │
   │         diff > 0 -> EXCESO              │
   │         diff < 0 -> FALTANTE            │
   │         else -> COINCIDE                │
   │       }                                 │
   │     ))                                  │
   │   }                                     │
   │                                         │
   │   // Añadir faltantes (no escaneados)  │
   │   referencia.values                     │
   │     .filter { it.codigo !in escaneados }│
   │     .forEach { ref ->                   │
   │       diferencias.add(DiferenciaInventario(│
   │         cantidadReferencia = ref.cantidad,│
   │         cantidadActual = 0,             │
   │         diferencia = -ref.cantidad,     │
   │         estado = FALTANTE               │
   │       ))                                │
   │     }                                   │
   │                                         │
   │   diferencias.sortedBy {...}            │
   │ }                                       │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
8. STATEFLOW EN SHAREDVIEWMODEL ACTUALIZA
   ┌─────────────────────────────────────────┐
   │ val comparacion: StateFlow<List<...>> = │
   │   manager.comparacionEnTiempoReal       │
   │     .stateIn(viewModelScope,            │
   │              SharingStarted.WhileSubscribed(5000),│
   │              emptyList())               │
   │                                         │
   │ ✅ Emite nueva lista a todos los        │
   │    observers (ambas actividades)        │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
9. ACTIVIDADES RECIBEN ACTUALIZACIÓN
   ┌─────────────────────────────────────────┐
   │ // En InventarioComposeActivity         │
   │ val ultimaDiferencia by                 │
   │   sharedViewModel.ultimoItemEscaneado   │
   │     .collectAsState()                   │
   │                                         │
   │ // En ComparacionTiempoRealActivity     │
   │ val comparacion by sharedViewModel      │
   │   .comparacion.collectAsState()         │
   │                                         │
   │ ✅ UI se repinta automáticamente        │
   └──────────────┬──────────────────────────┘
                  │
                  ▼
10. UI MUESTRA RESULTADOS
    ┌─────────────────────────────────────────┐
    │ InventarioComposeActivity:              │
    │   • AlertaDiscrepanciaOverlay           │
    │     - EXCESO: Rojo + vibración doble    │
    │     - FALTANTE: Naranja + vibración     │
    │                                         │
    │ ComparacionTiempoRealActivity:          │
    │   • ResumenComparativaCardPremium       │
    │     - Efectividad: 85%                  │
    │     - SKUs pendientes: 15               │
    │     - Velocidad: 12.5 SKU/min           │
    │   • Lista de diferencias                │
    │     - EXCESO (rojo)                     │
    │     - FALTANTE (naranja)                │
    │     - COINCIDE (verde)                  │
    └─────────────────────────────────────────┘

TIEMPO TOTAL: <100ms (dependiendo de velocidad de Room)
```

---

## Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         JERARQUÍA DE CLASES                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ InventarioComposeActivity           ComparacionTiempoRealActivity       │
│─────────────────────────────────     ─────────────────────────────────  │
│ - legacyViewModel                   │ - sharedViewModel                 │
│ - composeViewModel                  │                                   │
│ + sharedViewModel                   │                                   │
│─────────────────────────────────     ─────────────────────────────────  │
│ + onCreate()                        │ + onCreate()                      │
│ + procesarResultadoEscaneo()        │ + exportarAjusteSAP()             │
└────────────────────┬────────────────┴────────────────┬──────────────────┘
                     │ activityViewModels()            │
                     └───────────────┬─────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   InventarioSharedViewModel                             │
│─────────────────────────────────────────────────────────────────────────│
│ - repository: InventarioRepository                                      │
│ + manager: InventarioTiempoRealManager                                  │
│─────────────────────────────────────────────────────────────────────────│
│ + comparacion: StateFlow<List<DiferenciaInventario>>                    │
│ + ultimoItemEscaneado: StateFlow<DiferenciaInventario?>                 │
│ + estadisticas: StateFlow<EstadisticasSesion>                           │
│ + diferenciasCriticas: StateFlow<List<DiferenciaInventario>>            │
│─────────────────────────────────────────────────────────────────────────│
│ + registrarScan(codigo, descripcion, cantidad)                          │
│ + cargarInventarioReferencia(lista)                                     │
│ + limpiarInventarioEscaneado()                                          │
│ + eliminarItemEscaneado(codigo)                                         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  InventarioTiempoRealManager                            │
│─────────────────────────────────────────────────────────────────────────│
│ - repository: InventarioRepository                                      │
│ - _inventarioReferencia: MutableStateFlow<Map<String, ItemReferencia>> │
│─────────────────────────────────────────────────────────────────────────│
│ + comparacionEnTiempoReal: Flow<List<DiferenciaInventario>>             │
│ + ultimoItemEscaneado: Flow<DiferenciaInventario?>                      │
│ + diferenciasCriticas: Flow<List<DiferenciaInventario>>                 │
│─────────────────────────────────────────────────────────────────────────│
│ + cargarInventarioSistema(lista: List<ItemReferencia>)                  │
│ + getInventarioReferencia(): Map<String, ItemReferencia>                │
│ + tieneReferenciaCargada(): Boolean                                     │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ combine()
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    InventarioRepository                                 │
│─────────────────────────────────────────────────────────────────────────│
│ - dao: InventarioDao                                                    │
│─────────────────────────────────────────────────────────────────────────│
│ + observarEscaneados(): Flow<List<InventarioEscaneadoEntity>>           │
│ + observarItemPorCodigo(codigo): Flow<InventarioEscaneadoEntity?>       │
│ + contarItemsEscaneados(): Flow<Int>                                    │
│─────────────────────────────────────────────────────────────────────────│
│ + registrarScan(codigo, descripcion, cantidad): suspend                 │
│ + registrarLote(items): suspend                                         │
│ + limpiarInventario(): suspend                                          │
│ + eliminarItem(codigo): suspend                                         │
│ + obtenerTodosLosEscaneados(): suspend                                  │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       InventarioDao (Room)                              │
│─────────────────────────────────────────────────────────────────────────│
│ @Query Flow<List<..>> observarInventarioEscaneado()                     │
│ @Query Flow<..> observarItemPorCodigo(codigo)                           │
│ @Query suspend obtenerTodosLosEscaneados(): List<..>                    │
│ @Query suspend obtenerItemPorCodigo(codigo): ..?                        │
│ @Insert suspend insertarOActualizar(item): Long                         │
│ @Insert suspend insertarLista(items)                                    │
│ @Query suspend limpiarInventario()                                      │
│ @Query suspend eliminarItemPorCodigo(codigo)                            │
│ @Query Flow<Int> contarItemsEscaneados()                                │
│ @Query suspend contarItemsEscaneadosSincrono(): Int                     │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              TABLE: inventario_escaneado (SQLite)                       │
│─────────────────────────────────────────────────────────────────────────│
│ codigo: TEXT PRIMARY KEY                                                │
│ descripcion: TEXT NOT NULL                                              │
│ cantidadTotal: INTEGER NOT NULL                                         │
│ ultimoScanTimestamp: INTEGER NOT NULL                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Modelo de Datos

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATA CLASSES PRINCIPALES                        │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ InventarioEscaneadoEntity (Room Entity)                                 │
│─────────────────────────────────────────────────────────────────────────│
│ @Entity(tableName = "inventario_escaneado")                             │
│─────────────────────────────────────────────────────────────────────────│
│ + codigo: String (PRIMARY KEY)                                          │
│ + descripcion: String                                                   │
│ + cantidadTotal: Int                                                    │
│ + ultimoScanTimestamp: Long                                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ DiferenciaInventario (Data Class)                                       │
│─────────────────────────────────────────────────────────────────────────│
│─────────────────────────────────────────────────────────────────────────│
│ + codigo: String                                                        │
│ + descripcion: String                                                   │
│ + cantidadReferencia: Int                                               │
│ + cantidadActual: Int                                                   │
│ + diferencia: Int (= actual - referencia)                               │
│ + timestamp: Long                                                       │
│ + estado: EstadoDiferencia (EXCESO, FALTANTE, COINCIDE)                 │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ EstadoDiferencia (Enum)                                                 │
│─────────────────────────────────────────────────────────────────────────│
│ enum class EstadoDiferencia {                                           │
│   COINCIDE,      // diferencia == 0                                     │
│   FALTANTE,      // diferencia < 0                                      │
│   EXCESO         // diferencia > 0                                      │
│ }                                                                       │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ InventarioTiempoRealManager.ItemReferencia (Data Class)                 │
│─────────────────────────────────────────────────────────────────────────│
│─────────────────────────────────────────────────────────────────────────│
│ + codigo: String                                                        │
│ + descripcion: String                                                   │
│ + cantidad: Int                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ EstadisticasSesion (Data Class)                                         │
│─────────────────────────────────────────────────────────────────────────│
│─────────────────────────────────────────────────────────────────────────│
│ + totalSKUs: Int                                                        │
│ + coincidencias: Int                                                    │
│ + faltantes: Int                                                        │
│ + excesos: Int                                                          │
│ + efectividad: Int (0-100%)                                             │
│ + itemsEscaneados: Int                                                  │
│ + totalSistema: Int                                                     │
│ + skusPendientes: Int                                                   │
│ + timestampCalculo: Long                                                │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│ ResumenAjustes (Data Class para SAP)                                    │
│─────────────────────────────────────────────────────────────────────────│
│─────────────────────────────────────────────────────────────────────────│
│ + totalAjustes: Int                                                     │
│ + entradas701: Int (movimientos de entrada)                             │
│ + salidas702: Int (movimientos de salida)                             │
│ + cantidadEntrada: Int                                                  │
│ + cantidadSalida: Int                                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Patrón de Diseño: Observer con Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    IMPLEMENTACIÓN DEL PATRÓN OBSERVER                   │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Subject    │────────►│   Observer   │◄────────│   Observer   │
│   (Room DAO) │  Flow   │ (Activity 1) │         │ (Activity 2) │
└──────────────┘         └──────────────┘         └──────────────┘
       │                         │                        │
       │ 1. INSERT/UPDATE/DELETE │                        │
       ▼                         │                        │
┌──────────────┐                 │                        │
│ Invalidation │                 │                        │
│   Trigger    │                 │                        │
└──────┬───────┘                 │                        │
       │                        │                        │
       │ 2. Notify Observers    │                        │
       ▼                        ▼                        ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Flow<List<InventarioEscaneadoEntity>>         │
│                                                                  │
│  Emite automáticamente cada vez que hay un cambio en la BD      │
└──────────────────────────────────────────────────────────────────┘
       │                        │                        │
       │ 3. Combine + Transform │                        │
       ▼                        ▼                        ▼
┌──────────────────────────────────────────────────────────────────┐
│              InventarioTiempoRealManager                         │
│                                                                  │
│  combine(referencia, escaneados) { ref, esc ->                   │
│    // Calcular diferencias                                       │
│    List<DiferenciaInventario>                                    │
│  }                                                               │
└──────────────────────────────────────────────────────────────────┘
       │
       │ 4. StateFlow (State Holder)
       ▼
┌──────────────────────────────────────────────────────────────────┐
│              InventarioSharedViewModel                           │
│                                                                  │
│  val comparacion: StateFlow<List<DiferenciaInventario>> =        │
│    manager.comparacionEnTiempoReal                               │
│      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),│
│               emptyList())                                       │
└──────────────────────────────────────────────────────────────────┘
       │                        │                        │
       │ 5. collectAsState()    │                        │
       ▼                        ▼                        ▼
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Composable  │         │  Composable  │         │  Composable  │
│    UI 1      │         │    UI 2      │         │    UI 3      │
│  (Repaint)   │         │  (Repaint)   │         │  (Repaint)   │
└──────────────┘         └──────────────┘         └──────────────┘

VENTAJAS:
✅ Sin polling (no hay delays artificiales)
✅ Sin batería desperdiciada
✅ Actualización instantánea (<100ms)
✅ Múltiples observers sincronizados
✅ Lifecycle-aware (se detiene cuando no hay observers)
```

---

**Documentación técnica completa disponible en:**
- `ARQUITECTURA_REACTIVA_IMPLEMENTACION.md`
- `GUIA_INTEGRACION_ACTIVIDADES.md`
- `RESUMEN_IMPLEMENTACION.md`
