<div align="center">

<img src="https://img.shields.io/badge/DEMO%20CORP-Operations-E61C2E?style=for-the-badge&logoColor=white"/>

# 📦 InventoryScannerPro

**Sistema de Gestión de Inventarios en Tiempo Real**  
*Plataforma empresarial para captura, comparación y análisis de inventario físico en plantas PLANTA DE MANUFACTURA*

---

[![Android](https://img.shields.io/badge/Android-API%2024+-34A853?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%2B%20Auth-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Room](https://img.shields.io/badge/Room-DB%20v10-003B8E?style=flat-square)](https://developer.android.com/jetpack/androidx/releases/room)
[![Build](https://img.shields.io/badge/Build-Passing-2ECC71?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)

</div>

---

## 🎯 ¿Qué es InventoryScannerPro?

InventoryScannerPro es una aplicación Android de nivel empresarial diseñada específicamente para las operaciones de almacén de **PLANTA DE MANUFACTURA planta Norte**. Permite a los operadores realizar inventarios físicos mediante escaneo de códigos QR, compararlos en tiempo real contra un **inventario de referencia importado (CSV/Excel exportado desde SAP u otra fuente)**, y detectar desviaciones al instante.

> **Nota de honestidad técnica:** la app no se conecta a un API SAP en vivo. La “comparación SAP” usa un archivo de referencia local o sincronizado (CSV), no una integración RFC/OData con SAP ERP.

### Capacidades principales

| Función | Descripción |
|---|---|
| 📸 **Escaneo QR** | Captura de pallets con cámara en tiempo real |
| ⚖️ **Comparación vs referencia** | Contraste inmediato vs. inventario de referencia (CSV/Excel) |
| 🏭 **Catálogos por Planta** | Configuración de CxP, estibaje y TPM por ubicación |
| ⏱️ **Monitor TPM** | Alertas automáticas de vida útil por lote y fecha |
| 📊 **Resumen Ejecutivo** | Dashboard con KPIs, efectividad y gráficas de ocupación |
| ☁️ **Sincronización Firebase** | Subida/descarga de inventario vía Firestore; cola local `PENDING`/`SENT`/`FAILED` en Room (sin resolución de conflictos multi-dispositivo avanzada) |
| 📤 **Exportación** | Generación de reportes CSV/Excel compartibles |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────┐
│               UI Layer (Compose)                │
│  SplashActivity → LoginActivity → MainCompose   │
│  ├── InventarioComposeActivity (Escaneo)        │
│  ├── ComparacionTiempoRealActivity              │
│  ├── InventarioResumenComposeActivity           │
│  ├── CatalogoPlantaActivity ← NUEVO             │
│  └── MasOpcionesActivity                        │
├─────────────────────────────────────────────────┤
│           ViewModel Layer (StateFlow)           │
│  InventarioSharedViewModel (fuente de verdad)   │
│  ├── InventarioTiempoRealManager                │
│  ├── TPMMonitorManager                          │
│  └── CatalogoPlantaViewModel                    │
├─────────────────────────────────────────────────┤
│           Data Layer (Room + Firebase)          │
│  AppDatabase v10 (Room)                         │
│  ├── inventario_escaneado                       │
│  ├── catalogos_planta ← NUEVO                   │
│  ├── tpm_data                                   │
│  └── traspasos                                  │
│  FirebaseRepository (Firestore)                 │
└─────────────────────────────────────────────────┘
```

**Patrón:** MVVM + Repository + Reactive Flows  
**Concurrencia:** Kotlin Coroutines + StateFlow  
**UI:** Jetpack Compose + Material3, con algunas vistas XML heredadas en migración

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material3 | Compose UI 1.7 / Material3 1.3 |
| Arquitectura | AndroidViewModel + StateFlow | - |
| Base de datos local | Room | 2.8.4 |
| ORM generador | KSP | - |
| Backend / Sync | Firebase Firestore + Authentication | BOM 34.15.0 |
| Escaneo | ZXing / ML Kit | - |
| Build | Gradle + Kotlin DSL | 9.2.1 |
| IDE recomendado | Android Studio Ladybug+ | - |

---

## 🚀 Instalación y Compilación

### Requisitos previos

- **Android Studio** Ladybug o superior
- **JDK** 17 o 21 (JDK 21 recomendado)
- **Android SDK** 34+
- **Archivo `google-services.json`** (no incluido, solicitarlo al administrador)

### Clonar el repositorio

```bash
git clone https://github.com/Joseluiscruz-hub/InventoryScannerPro.git
cd InventoryScannerPro
```

### Compilar en Windows

```bat
# Limpiar y compilar APK debug
.\gradlew.bat clean assembleDebug --no-daemon

# Instalar directamente en dispositivo conectado
.\gradlew.bat installDebug --no-daemon
```

### Compilar en Linux / macOS

```bash
./gradlew clean assembleDebug --no-daemon
./gradlew installDebug --no-daemon
```

**Ruta del APK generado:**

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Flujo de Navegación

```
SplashActivity (logo + animación)
    └── LoginActivity (autenticación)
            └── MainComposeActivity (menú principal)
                    ├── InventarioNavHostActivity → Escaneo de pallets
                    ├── ComparacionTiempoRealActivity → Diferencias vs referencia
                    ├── InventarioResumenComposeActivity → KPIs y gráficas
                    ├── DashboardComparacionActivity → Vista general
                    ├── CatalogoPlantaActivity → Gestión de catálogos
                    ├── TraspasoActivity → Movimientos entre almacenes
                    ├── ExportarActivity → Exportar CSV/Excel
                    └── MasOpcionesActivity → Configuración y utilidades
```

---

## 🏭 Módulo de Catálogos por Planta

Este módulo permite gestionar las configuraciones específicas de cada planta de producción:

### Campos del catálogo (Formato Norte)

| Campo | Descripción |
|---|---|
| `SKU` | Código de producto SAP |
| `Descripción` | Nombre comercial del producto |
| `Tamaño (ml)` | Volumen de la botella |
| `Empaque` | Tipo de envase (PET NR, PET Ret, etc.) |
| `Marca` | Marca del producto |
| `Botellas x Caja` | Unidades por caja |
| `Segmento` | Categoría de mercado |
| `TPM (días)` | Vida útil para alertas de TPM |
| `Cajas x Tarima` | ⭐ Valor crítico para conversión de inventario |
| `Camas x Tarima` | Capas por tarima |
| `Nivel de Estiba` | Estibas máximas permitidas |

### Importación rápida desde Excel

1. Copia el rango de celdas directamente desde Excel (Ctrl+C)
2. En la app: **Más Opciones → Catálogos por Planta → "+"**
3. Escribe el nombre de la planta (ej. `Norte`)
4. Pega el contenido y presiona **IMPORTAR**

El sistema reconoce automáticamente tanto **tabuladores** (Excel) como **comas** (CSV).

---

## 📊 Base de Datos Local

La base de datos Room está en la **versión 10** con historial de migraciones preservado:

| Versión | Cambio principal |
|---|---|
| v1 → v2 | Compatibilidad inicial |
| v3 → v4 | Tabla de traspasos |
| v4 → v5 | Inventario escaneado |
| v5 → v6 | Modelo de eventos (append-only) |
| v6 → v7 | Campos TPM y fechas de caducidad |
| v7 → v8 | Tabla `tpm_data` con índices |
| **v8 → v9** | **Tabla `catalogos_planta` (multi-planta)** |
| **v9 → v10** | **Soporte de restos y tipo de tarima en inventario escaneado** |

> ⚠️ **Nunca uses `fallbackToDestructiveMigration()` en producción.** Las migraciones están definidas explícitamente para preservar el historial de inventarios.

---

## 🔒 Seguridad

> La aplicación Android se considera un cliente potencialmente manipulable.
> La autorización, las reglas de negocio y las operaciones que modifican
> inventario deben validarse fuera del APK.

### Distribución y firma

- Los builds de producción se generan como `release`, con `debuggable=false`,
  R8/ProGuard y reducción de recursos habilitados.
- El APK/AAB se firma con un keystore de producción único. El archivo `.jks`,
  sus contraseñas y `keystore.properties` nunca se suben al repositorio.
- Toda actualización de producción debe conservar la misma clave de firma.
- Antes de distribuir, se verifica la firma del artefacto con `apksigner`.

### Firebase y autorización

- Firebase Authentication identifica al usuario; no autoriza por sí solo.
- Los roles, la planta y el alcance operativo se obtienen de claims verificados
  o de una fuente controlada por servidor, nunca de valores enviados por la app.
- Firestore Security Rules deben negar por defecto y validar autenticación,
  rol, planta y propiedad de cada documento.
- Un operador no puede consultar ni modificar inventario, catálogos, TPM,
  traspasos o exportaciones de otra planta.
- Las operaciones de alto impacto —ajustes, traspasos, sincronización,
  cambios de stock y acciones administrativas— pasan por Cloud Functions o
  una API backend que valida permisos e inputs y ejecuta transacciones.
- La aplicación cliente no debe escribir directamente un stock final calculado.

### Modelo de autoridad

El APK puede ser inspeccionado o modificado por quien lo instala. Por ello,
la aplicación nunca es la fuente de autoridad para roles, planta, stock final
ni permisos. El cliente solicita una operación; Firestore/Cloud Functions
validan la identidad autenticada, el alcance de la planta, la cantidad y las
reglas de negocio antes de actualizar datos.

### Datos, red y exportaciones

- La comunicación de producción utiliza HTTPS; se bloquea tráfico HTTP plano
  mediante `network_security_config`.
- Las exportaciones se generan mediante Storage Access Framework y
  `FileProvider`, sin permisos legacy amplios de almacenamiento.
- La base Room local no guarda contraseñas, tokens administrativos ni
  credenciales de Firebase.
- Los datos sensibles que requieran almacenamiento local se minimizan,
  se eliminan al cerrar sesión cuando aplique y no se incluyen en backups
  no controlados.
- No se utiliza `fallbackToDestructiveMigration()` en producción.

### Secretos y configuración

- `google-services.json` identifica la configuración Firebase del cliente;
  no sustituye las reglas de acceso y no debe contener secretos administrativos.
- Archivos de cuenta de servicio, llaves privadas, tokens administrativos,
  contraseñas y claves de servicios externos no se incluyen en el APK ni
  se suben a Git.
- Toda API key de cliente se restringe por package name y certificado
  SHA-1/SHA-256 cuando el servicio lo permita.
- Si se detecta una clave privada, secreto o token administrativo expuesto,
  se revoca y rota de inmediato.

### Validación antes de producción

- Se prueba el release firmado en un dispositivo limpio.
- Se verifica que un usuario sin sesión no pueda leer ni escribir datos.
- Se prueban roles de operador, supervisor y administrador con cuentas ficticias.
- Se intenta acceso cruzado entre plantas y modificación directa de stock.
- Se comprueba que cantidades inválidas, movimientos duplicados y operaciones
  sin permisos sean rechazados y auditados.

> **Estado actual (auditoría):** App Check (debug + Play Integrity) está cableado en código.
> Cloud Functions como autoridad de stock **aún no** están en el repositorio; ver
> [`docs/SECURITY_AUDIT.md`](docs/SECURITY_AUDIT.md) para gaps vs. este modelo objetivo.

---

## 📂 Estructura del Proyecto

```
app/src/main/java/com/joseluiscruz/capturainventario/
├── data/
│   ├── AppDatabase.kt              # Room DB v10, migraciones 1-10
│   ├── CatalogoPlantaEntity.kt     # Entidad catálogos por planta
│   ├── CatalogoPlantaDao.kt        # DAO catálogos
│   ├── ProductoTerminadoCatalogo.kt # Catálogo estático + configuración dinámica
│   └── repository/
│       ├── InventarioRepository.kt
│       └── CatalogoPlantaRepository.kt
├── ui/
│   ├── splash/                     # SplashActivity
│   ├── login/                      # LoginActivity
│   ├── main/                       # MainComposeActivity (menú)
│   ├── inventario/
│   │   ├── screen/                 # Escaneo, Resumen, Catálogos
│   │   └── viewmodel/              # CatalogoPlantaViewModel
│   ├── comparacion/                # Comparación en tiempo real
│   ├── opciones/                   # Más Opciones
│   ├── exportar/                   # Exportación CSV
│   └── theme/                      # PremiumTheme + BrandColors
├── viewmodel/
│   ├── InventarioSharedViewModel.kt # Fuente única de verdad
│   └── InventoryComparisonViewModel.kt
└── util/
    └── CsvParser.kt                # Parser defensivo (testeable)
```

---

## 📋 Documentación Técnica

Toda la documentación se encuentra en la carpeta [`docs/`](docs/):

| Documento | Descripción |
|---|---|
| [`ARQUITECTURA_REACTIVA_IMPLEMENTACION.md`](docs/ARQUITECTURA_REACTIVA_IMPLEMENTACION.md) | Flows, StateFlows y reactividad |
| [`DIAGRAMA_ARQUITECTURA.md`](docs/DIAGRAMA_ARQUITECTURA.md) | Diagrama completo de capas |
| [`GUIA_DE_USO.md`](docs/GUIA_DE_USO.md) | Manual del operador |
| [`MIGRACION_COMPOSE_PREMIUM.md`](docs/MIGRACION_COMPOSE_PREMIUM.md) | Guía de migración Views → Compose |
| [`CSV_TEST_FILES.md`](docs/CSV_TEST_FILES.md) | Formato de archivos CSV de referencia |
| [`TEST_CHECKLIST.md`](docs/TEST_CHECKLIST.md) | Checklist de pruebas manuales |
| [`SECURITY_AUDIT.md`](docs/SECURITY_AUDIT.md) | Auditoría de seguridad (gaps vs. modelo objetivo) |
| [`FIREBASE_ROLES.md`](docs/FIREBASE_ROLES.md) | Roles y Custom Claims |

---

## ❓ Solución de Problemas

### El build falla con errores de recursos

```bash
# Sincronizar Gradle desde Android Studio:
File → Sync Project with Gradle Files
```

### Error de migración de base de datos

Si ves `IllegalStateException: Room cannot verify the data integrity`, la base de datos en el dispositivo está en una versión incompatible. En desarrollo puedes limpiar datos de la app:

```bash
adb shell pm clear com.joseluiscruz.capturainventario
```

### Símbolos extraños en el inventario de referencia

El parser CSV utiliza **UTF-8 explícito** con soporte para BOM de Excel. Si persiste el problema, verifica que el archivo exportado (p. ej. desde SAP) esté guardado en formato **UTF-8** (no ANSI/Latin-1).

### El wrapper no descarga dependencias

Verifica la conexión a Internet y la configuración de proxy. Si estás detrás de un firewall corporativo, configura las variables `GRADLE_OPTS` con los datos del proxy.

---

## 🤝 Contribución

Este es un proyecto personal de  Jose Luis Cruz, para uso de **PLANTA DE MANUFACTURA — Operaciones México**. Para reportar bugs o solicitar nuevas funcionalidades, contacta a 141293124+Joseluiscruz-hub@users.noreply.github.com o usa la opción **"Reportar Error / Feedback"** dentro de la aplicación.

> Planta de Manufactura es tenant de laboratorio (Microsoft 365 E5). No es el cliente ni el empleador.

---

<div align="center">

**© 2026 PLANTA DE MANUFACTURA · Operaciones México**  
*Todos los derechos reservados.*

*Desarrollado para optimizar el control de inventarios físicos en plantas de producción.*

</div>
