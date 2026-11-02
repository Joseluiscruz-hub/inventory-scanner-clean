# 📖 Guía de Uso - EscaneoDemo Enterprise

## 🎯 Descripción General

**EscaneoDemo** es una aplicación empresarial para gestión de inventarios con validación en tiempo real contra SAP. Incluye alertas visuales tipo "Dynamic Island", feedback háptico inteligente, y exportación directa a SAP MM.

**Versión:** 2.0.0 Enterprise  
**Última actualización:** 14 de marzo de 2026

---

## 📱 Requisitos del Sistema

### Hardware:
- **Dispositivo:** Samsung S22 Ultra o superior (recomendado)
- **Cámara:** Autofocus requerido
- **Almacenamiento:** 100 MB libres mínimos
- **RAM:** 4 GB recomendados

### Software:
- **Android:** 10.0 (API 29) o superior
- **Play Services:** Requerido para escaneo QR
- **Permisos:** Cámara, Almacenamiento, Notificaciones

---

## 🚀 Primeros Pasos

### 1. Iniciar Sesión

```
┌─────────────────────────────────┐
│     ESCANEO PLANTA DE MANUFACTURA ENTERPRISE      │
│                                 │
│  👤 Usuario: _______________    │
│  🔒 Contraseña: ____________    │
│                                 │
│  [        INGRESAR        ]     │
└─────────────────────────────────┘
```

- Ingresa tus credenciales corporativas
- Presiona **INGRESAR**
- Serás redirigido al Dashboard principal

### 2. Dashboard Principal

```
┌─────────────────────────────────┐
│  📊 DASHBOARD                   │
│                                 │
│  ┌──────────┐  ┌──────────┐    │
│  │ 📦       │  │ 🔍       │    │
│  │ Capturar │  │ Comparar │    │
│  │          │  │          │    │
│  └──────────┘  └──────────┘    │
│                                 │
│  ┌──────────┐  ┌──────────┐    │
│  │ 📤       │  │ ⚙️       │    │
│  │ Exportar │  │ Ajustes  │    │
│  │          │  │          │    │
│  └──────────┘  └──────────┘    │
└─────────────────────────────────┘
```

---

## 📦 FASE 1: Captura de Inventario con HUD

### Paso 1: Seleccionar Almacén

1. Presiona el ícono **🏭 Almacén** en la esquina superior derecha
2. Selecciona tu almacén de la lista:
   - **0001** - Almacén Central
   - **0002** - Almacén Norte
   - **0003** - Almacén Sur
   - **0004** - Almacén Oriente
   - **0005** - Almacén Poniente

### Paso 2: Seleccionar Modo de Escaneo

```
┌─────────────────────────────────┐
│  MODO DE ESCANEO                │
│                                 │
│  ⚪ DEFAULT   - Escaneo simple  │
│  ⚪ PALLET    - Con tarimas     │
│  ⚪ RESTOS    - Material suelto │
│  ⚪ TRASPASO  - Entre almacenes │
│  ⚪ MANUAL    - Captura manual  │
└─────────────────────────────────┘
```

**Recomendación:** Usa **DEFAULT** para la mayoría de los casos.

### Paso 3: Escanear Código de Barras

1. Presiona el botón **[ 📷 ESCANEAR ]**
2. Apunta la cámara al código de barras
3. La aplicación leerá automáticamente

#### Resultado del Escaneo - HUD Dynamic Island

**Inmediatamente después de escanear, verás:**

```
┌─────────────────────────────────────────┐
│                                         │
│  ╔═══════════════════════════════════╗ │
│  ║ ⚠️ ¡EXCESO DETECTADO!             ║ │
│  ║ SKU12345                          ║ │
│  ║ Material Description Here         ║ │
│  ║                    SAP: 100       ║ │
│  ║              +15 → 115            ║ │
│  ╚═══════════════════════════════════╝ │
│                                         │
└─────────────────────────────────────────┘
```

### Estados del HUD y Significados

#### ✅ COINCIDENCIA PERFECTA (Verde)
```
╔═══════════════════════════════════╗
║ ✅ COINCIDENCIA PERFECTA          ║
║ SKU12345                          ║
║ Material Description              ║
║                    SAP: 100       ║
║                   ✓ → 100        ║
╚═══════════════════════════════════╝
```
- **Significado:** Lo escaneado coincide exactamente con SAP
- **Vibración:** Simple (confirmación suave)
- **Acción:** Continuar escaneando

#### ⚠️ FALTANTE REGISTRADO (Naranja)
```
╔═══════════════════════════════════╗
║ ⚠️ FALTANTE REGISTRADO            ║
║ SKU12345                          ║
║ Material Description              ║
║                    SAP: 100       ║
║               -25 → 75            ║
╚═══════════════════════════════════╝
```
- **Significado:** Escaneaste menos de lo que SAP indica
- **Vibración:** Media (advertencia)
- **Acción:** Verificar si hay más material en la ubicación

#### 🚨 ¡EXCESO DETECTADO! (Rojo)
```
╔═══════════════════════════════════╗
║ 🚨 ¡EXCESO DETECTADO!             ║
║ SKU12345                          ║
║ Material Description              ║
║                    SAP: 100       ║
║              +15 → 115            ║
╚═══════════════════════════════════╝
```
- **Significado:** Escaneaste más de lo que SAP indica
- **Vibración:** Doble y fuerte (alerta crítica)
- **Acción:** **DETENERSE** - Verificar si hay error de captura o material no registrado

### Características del HUD

| Característica | Descripción |
|----------------|-------------|
| **Auto-ocultado** | Desaparece después de 3.5 segundos |
| **Animación** | Entrada/salida suave tipo spring |
| **Posición** | Parte superior central (Dynamic Island) |
| **Información** | SKU, descripción, SAP vs Real, diferencia |
| **Z-Index** | Siempre sobre el contenido |

### Feedback Háptico

| Estado | Patrón de Vibración |
|--------|---------------------|
| **Coincide** | `Vibración larga simple` |
| **Faltante** | `Vibración media` |
| **Exceso** | `Vibración larga` → `150ms` → `Vibración larga` |

### Paso 4: Captura Manual (Opcional)

Si necesitas ingresar datos manualmente:

1. **SKU:** Ingresa el código del material
2. **Ubicación:** Especifica la ubicación en almacén
3. **Total Pallets:** Cantidad de tarimas
4. **Restos:** Material suelto (si aplica)
5. Presiona **[ 💾 GUARDAR ]**

### Paso 5: Utilidades Adicionales

```
┌─────────────────────────────────┐
│  UTILIDADES                     │
│                                 │
│  [📊 Resumen] [📤 Exportar]    │
│  [♻️ Restos]  [🔍 Comparar]    │
└─────────────────────────────────┘
```

- **📊 Resumen:** Ver totales capturados
- **📤 Exportar:** Generar archivo CSV
- **♻️ Restos:** Capturar material suelto
- **🔍 Comparar:** Ir a pantalla de comparación

---

## 🔍 FASE 2: Centro de Mando (Auditoría)

### Acceso al Centro de Mando

1. Desde el Dashboard, presiona **[ 🔍 Comparar ]**
2. O desde Captura, presiona **[ 🔍 Comparar ]** en Utilidades

### Vista Principal del Centro de Mando

```
┌─────────────────────────────────┐
│  📊 AUDITORÍA DE INVENTARIO     │
│                                 │
│         ╭──────────────╮        │
│        ╱   ╭─────╮     ╲       │
│       │    │ 85% │      │      │
│       │    │Limpio│     │      │
│        ╲   ╰─────╯     ╱       │
│         ╰──────────────╯        │
│                                 │
│  [Todos] [Excesos 🔴]           │
│  [Faltantes 🟡] [Completos 🟢] │
│                                 │
│  📦 850   ✅ 820   ⚠️ 15   🔴 15│
│                                 │
│  ╔═══════════════════════════╗ │
│  ║ SKU12345    +15  EXCESO  ║ │
│  ║ SKU67890    -5   FALTANTE║ │
│  ║ SKU11111    ✓    OK      ║ │
│  ╚═══════════════════════════╝ │
│                                 │
│              [📤 EXPORTAR]      │
└─────────────────────────────────┘
```

### Dona de Glassmorphism

La dona central muestra visualmente el estado del inventario:

- **🟢 Verde:** Coincidencias (inventario limpio)
- **🟡 Naranja:** Faltantes (material por encontrar)
- **🔴 Rojo:** Excesos (material no registrado)
- **Centro:** Porcentaje de efectividad

**Animación:** La dona se llena progresivamente en 1.2 segundos al cargar.

### Filtros Rápidos en Píldoras

Presiona los chips para filtrar la lista:

| Filtro | Función |
|--------|---------|
| **[Todos]** | Muestra todos los items |
| **[Excesos 🔴]** | Solo items con cantidad > SAP |
| **[Faltantes 🟡]** | Solo items con cantidad < SAP |
| **[Completos 🟢]** | Solo items que coinciden |

**Scroll:** Desliza horizontalmente si hay más filtros.

### Estadísticas Rápidas

Debajo de la dona, verás 4 indicadores:

| Ícono | Métrica | Descripción |
|-------|---------|-------------|
| 📦 | **Total** | Total de SKUs únicos |
| ✅ | **Coincidencias** | Items que cuadran con SAP |
| ⚠️ | **Faltantes** | Items con cantidad menor |
| 🔴 | **Excesos** | Items con cantidad mayor |

### Lista de Diferencias

Cada item en la lista muestra:

```
┌─────────────────────────────────┐
│  🟢  SKU12345        ✓         │
│      Material Description      │
│      Sistema: 100  Escaneado:  │
│                                │
│  🔴  SKU67890        +15       │
│      Otro Material             │
│      Sistema: 100  Escaneado:  │
└─────────────────────────────────┘
```

**Colores por estado:**
- **🟢 Verde:** COINCIDE
- **🔴 Rojo:** EXCESO
- **🟡 Naranja:** FALTANTE

---

## 📤 FASE 3: Exportación SAP (One-Click)

### Botón FAB Contextual

El botón flotante cambia según el estado:

#### Cuando hay diferencias:
```
┌─────────────────────────┐
│  ⚠️  EXPORTAR AJUSTE   │
│         SAP            │
└─────────────────────────┘
```
- **Color:** Rojo corporativo
- **Acción:** Genera CSV para SAP MM

#### Cuando todo está limpio:
```
┌─────────────────────────┐
│  ✅  AUDITORÍA LIMPIA  │
└─────────────────────────┘
```
- **Color:** Verde éxito
- **Acción:** Cierra sesión / Termina auditoría

### Proceso de Exportación

**Paso 1:** Presiona **[ 📤 EXPORTAR AJUSTE SAP ]**

**Paso 2:** Verás el diálogo de confirmación:
```
┌─────────────────────────────────┐
│     EXPORTAR AJUSTE SAP         │
│                                 │
│  Se generarán 30 ajustes:       │
│  • 15 entradas (movimiento 701) │
│  • 15 salidas (movimiento 702)  │
│                                 │
│  [Cancelar]  [Exportar CSV]    │
└─────────────────────────────────┘
```

**Paso 3:** Presiona **[ Exportar CSV ]**

**Paso 4:** Se abrirá el menú de compartir:
- 📧 Enviar por email
- 💬 Enviar por Teams
- 💾 Guardar en Downloads
- 📁 Guardar en OneDrive/Google Drive

### Formato del CSV Generado

El archivo se llama: `AJUSTE_SAP_[timestamp].csv`

**Contenido:**
```csv
SOCIEDAD;ALMACEN;MATERIAL;DESCRIPCION;TIPO_MOV;CANTIDAD;UNIDAD;FECHA
Demo1;0001;SKU12345;Descripcion del Material;701;5;UN;14.03.2026
Demo1;0001;SKU67890;Otro Material;702;3;UN;14.03.2026
```

**Campos:**
- **SOCIEDAD:** Demo1 (PLANTA DE MANUFACTURA)
- **ALMACEN:** Código de almacén (ej. 0001)
- **MATERIAL:** SKU del producto
- **DESCRIPCION:** Descripción del material
- **TIPO_MOV:** 
  - `701` = Entrada de mercancía (exceso físico)
  - `702` = Salida de mercancía (faltante físico)
- **CANTIDAD:** Valor absoluto de la diferencia
- **UNIDAD:** UN (unidades)
- **FECHA:** Formato dd.MM.yyyy

### Ubicación del Archivo

Los archivos se guardan en:
```
/Android/data/com.joseluiscruz.capturainventario/files/Documents/
```

**Nombre del archivo:**
```
AJUSTE_SAP_1710432000000.csv
```

---

## 🎯 Casos de Uso Comunes

### Caso 1: Inventario Físico Completo

**Objetivo:** Contar todo el almacén y ajustar SAP

1. **Inicio:** Dashboard → 📦 Capturar
2. **Configuración:** Seleccionar Almacén → DEFAULT
3. **Escaneo:** Escanear cada SKU en el almacén
4. **Validación:** Revisar HUD después de cada escaneo
5. **Auditoría:** Ir a 🔍 Comparar
6. **Filtrado:** Usar filtros para encontrar problemas
7. **Exportación:** Presionar FAB → Exportar CSV
8. **Envío:** Enviar CSV a sistemas@example.com

**Tiempo estimado:** 500 SKUs en ~30 minutos

### Caso 2: Búsqueda de Material Específico

**Objetivo:** Encontrar un material en el almacén

1. **Inicio:** Dashboard → 🔍 Comparar
2. **Filtro:** Presionar [Todos] o buscar manualmente
3. **Scroll:** Deslizar hasta encontrar el SKU
4. **Ubicación:** Ver columna "Ubicación" si está disponible

### Caso 3: Corrección de Exceso

**Objetivo:** Corregir un exceso detectado

1. **HUD muestra:** "¡EXCESO DETECTADO! +15"
2. **Acción inmediata:** Detener el escaneo
3. **Verificación:** 
   - ¿El material estaba en otra ubicación?
   - ¿Error de captura anterior?
   - ¿Material no registrado en SAP?
4. **Decisión:**
   - Si es real: Continuar (quedará registrado para ajuste 701)
   - Si es error: Eliminar captura y re-escanear

### Caso 4: Auditoría Parcial

**Objetivo:** Auditar solo una sección del almacén

1. **Filtro:** En Centro de Mando, usar [Faltantes 🟡]
2. **Ubicación:** Filtrar por ubicación específica
3. **Escaneo:** Solo escanear items faltantes
4. **Validación:** Verificar que la dona se vuelva verde

---

## ⚠️ Solución de Problemas

### Problema: HUD no aparece después de escanear

**Causa posible:** Repository no está inyectado

**Solución:**
1. Verificar que la app se haya instalado desde la versión más reciente
2. Reinstalar la aplicación
3. Reportar a TI si persiste

### Problema: Vibración no funciona

**Causa posible:** Permisos o configuración del dispositivo

**Solución:**
1. Ir a **Ajustes** → **Sonido y vibración**
2. Asegurar que **Vibración** esté activada
3. En la app, ir a **Ajustes** → **Feedback háptico** → Activar

### Problema: CSV no se exporta

**Causa posible:** Permisos de almacenamiento

**Solución:**
1. Ir a **Ajustes** → **Aplicaciones** → **EscaneoDemo**
2. **Permisos** → Activar **Archivos y contenido multimedia**
3. Reintentar exportación

### Problema: Dona no muestra datos

**Causa posible:** No se ha cargado el inventario de referencia (SAP)

**Solución:**
1. Cerrar sesión completamente
2. Volver a iniciar sesión
3. Verificar conexión a internet
4. Reportar a TI si persiste

---

## 📊 Métricas y KPIs

### Efectividad de Inventario

**Fórmula:**
```
Efectividad = (Coincidencias / Total SKUs) × 100
```

**Interpretación:**
- **95-100%:** Excelente ✅
- **85-94%:** Bueno 👍
- **70-84%:** Regular ⚠️
- **<70%:** Requiere atención 🚨

### Velocidad de Escaneo

**Meta:** 15-20 SKUs por minuto

**Factores que afectan:**
- Tamaño del almacén
- Organización del material
- Experiencia del operador
- Conexión de red (para validación SAP)

---

## 🔐 Seguridad y Buenas Prácticas

### Recomendaciones de Seguridad

1. **No compartir credenciales** con otros operadores
2. **Cerrar sesión** al terminar cada turno
3. **No exportar** a correos personales
4. **Reportar** discrepancias mayores al 10%

### Buenas Prácticas de Operación

1. **Antes de empezar:**
   - Verificar carga de batería (>50%)
   - Limpieza de lente de cámara
   - Conexión WiFi estable

2. **Durante el escaneo:**
   - Mantener el dispositivo estable
   - Esperar confirmación del HUD antes de siguiente escaneo
   - Revisar ubicación del material

3. **Al finalizar:**
   - Revisar Centro de Mando para discrepancias
   - Exportar CSV si hay diferencias
   - Cerrar sesión correctamente

---

## 📞 Soporte Técnico

### Contacto

| Departamento | Email | Teléfono |
|--------------|-------|----------|
| **TI Corporativo** | ti@example.com | Ext. 1234 |
| **Sistemas SAP** | sistemas@example.com | Ext. 5678 |
| **Soporte App** | soporte.app@example.com | Ext. 9012 |

### Información para Reportar

Al reportar un problema, incluye:

1. **Dispositivo:** Modelo y versión de Android
2. **Versión de la app:** Settings → Acerca de
3. **Descripción del problema:** Qué estabas haciendo cuando ocurrió
4. **Captura de pantalla:** Si es posible
5. **ID de sesión:** En Dashboard → Ayuda → ID Sesión

---

## 📝 Glosario de Términos

| Término | Definición |
|---------|------------|
| **HUD** | Heads-Up Display - Alerta visual flotante |
| **SAP** | Sistema de gestión empresarial |
| **SKU** | Stock Keeping Unit - Código único de producto |
| **Movimiento 701** | Entrada de mercancía en SAP |
| **Movimiento 702** | Salida de mercancía en SAP |
| **Glassmorphism** | Efecto visual de vidrio esmerilado |
| **Dynamic Island** | Notificación flotante tipo isla dinámica |
| **Haptic Feedback** | Retroalimentación táctil (vibración) |

---

## 🎉 Novedades de la Versión 2.0

### Mejoras Implementadas

- ✅ **HUD Dynamic Island** para validación inmediata
- ✅ **Feedback háptico** diferenciado por estado
- ✅ **Dona de auditoría** con efecto glassmorphism
- ✅ **Filtros rápidos** en píldoras
- ✅ **Exportación SAP** en un solo clic
- ✅ **Arquitectura reactiva** Room + Flow
- ✅ **Sincronización** en tiempo real entre pantallas

### Próximas Mejoras

- 🔄 Modo offline completo
- 📊 Reportes gráficos avanzados
- 🔔 Notificaciones push de auditoría
- 🌐 Integración directa con SAP RFC

---

**© 2026 PLANTA DE MANUFACTURA - Todos los derechos reservados**  
**Desarrollado por:** Equipo de Innovación TI  
**Versión del documento:** 1.0  
**Fecha de publicación:** 14 de marzo de 2026
