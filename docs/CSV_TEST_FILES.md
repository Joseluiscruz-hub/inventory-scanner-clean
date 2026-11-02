# 📊 Archivos CSV de Prueba para ImportarActivity

## Formato 1: Inventario Completo (INVENTARIO)

**Nombre del archivo:** `inventario_test.csv`

```csv
SKU,Descripcion,CxPal,FPC,Con,Centro,Linea,OP,FProd,DiasV,Ubicacion,TotalPallets,TipoTarima,Almacen
SKU001,Producto 1,12,100,50,CENTRO1,LINEA1,OP001,2024-01-01,5,Pasillo A-01,10,PLASTICO,ALMACEN1
SKU002,Producto 2,24,200,100,CENTRO1,LINEA2,OP002,2024-01-02,3,Pasillo B-01,5,MADERA,ALMACEN2
SKU003,Producto 3 con "comillas",6,150,75,CENTRO2,LINEA1,OP003,2024-01-03,7,Pasillo C-01,15,PLASTICO,ALMACEN1
SKU004,Producto con; semicolon,12,100,50,CENTRO2,LINEA2,OP004,2024-01-04,2,Pasillo D-01,8,METAL,ALMACEN3
```

---

## Formato 2: Tarimas y Conversiones (TARIMAS)

**Nombre del archivo:** `tarimas_test.csv`

```csv
SKU,Descripcion,Cantidad,CajasXPallet,Armado,TarimaPlasticaSku
SKU001,Producto Base 1,100,12,12,TAR-001
SKU002,Producto Base 2,200,24,24,TAR-002
SKU003,Producto Base 3,150,6,12,TAR-001
SKU004,Producto Base 4,300,12,24,TAR-003
SKU005,Producto Base 5,50,8,8,TAR-002
```

---

## Formato 3: Datos del Sistema (SISTEMA)

**Nombre del archivo:** `sistema_referencia.csv`

```csv
SKU,Descripcion,Cantidad,CajasXPallet,Armado,TarimaPlasticaSku
REF001,Referencia Sistema 1,500,12,12,TAR-SYS-001
REF002,Referencia Sistema 2,750,24,24,TAR-SYS-002
REF003,Referencia Sistema 3,300,6,12,TAR-SYS-001
REF004,Referencia Sistema 4,600,12,24,TAR-SYS-003
REF005,Referencia Sistema 5,200,8,8,TAR-SYS-002
REF006,Referencia Sistema 6,900,24,24,TAR-SYS-004
```

---

## Formato 4: Simplificado (Compatible Rumba)

**Nombre del archivo:** `inventario_rumba.csv`

```csv
SKU,Descripcion,CajasXPallet
SKU-RUMBA-001,Producto Rumba 1,12
SKU-RUMBA-002,Producto Rumba 2,24
SKU-RUMBA-003,Producto Rumba 3,6
```

---

## Formato 5: Con Separador de Punto y Coma (;)

**Nombre del archivo:** `tarimas_semicolon.csv`

```csv
SKU;Descripcion;Cantidad;CajasXPallet;Armado;TarimaPlasticaSku
SKU-SC-001;Producto Semicolon 1;100;12;12;TAR-SC-001
SKU-SC-002;Producto Semicolon 2;200;24;24;TAR-SC-002
SKU-SC-003;Producto Semicolon 3;150;6;12;TAR-SC-001
```

---

## Formato 6: Con Caracteres Especiales (Prueba de Robustez)

**Nombre del archivo:** `inventario_especial.csv`

```csv
SKU,Descripcion,CxPal,FPC,Con,Centro,Linea,OP,FProd,DiasV,Ubicacion,TotalPallets,TipoTarima,Almacen
SKU-ESP-001,"Producto con, comas",12,100,50,CENTRO1,LINEA1,OP001,2024-01-01,5,"Pasillo A-01, Nivel 2",10,PLASTICO,ALMACEN1
SKU-ESP-002,"Producto con ""comillas"" dobles",24,200,100,CENTRO1,LINEA2,OP002,2024-01-02,3,Pasillo B-01,5,MADERA,ALMACEN2
SKU-ESP-003,Producto con ñ y acentos: á é í ó ú,6,150,75,CENTRO2,LINEA1,OP003,2024-01-03,7,Pasillo C-01,15,PLASTICO,ALMACEN1
SKU-ESP-004,Producto (con) [caracteres],12,100,50,CENTRO2,LINEA2,OP004,2024-01-04,2,Pasillo D-01,8,METAL,ALMACEN3
```

---

## Cómo Crear Archivos de Prueba

### Opción 1: Usando Notepad++
1. Copia y pega el contenido CSV arriba
2. Guarda como `.csv` en encoding UTF-8
3. Transfiere al dispositivo Android

### Opción 2: Usando Excel/LibreOffice
1. Crea un archivo con los datos
2. Exporta como CSV (delimitado por comas)
3. Transfiere al dispositivo

### Opción 3: Transferir vía ADB
```bash
adb push inventario_test.csv /sdcard/Download/
adb push tarimas_test.csv /sdcard/Download/
adb push sistema_referencia.csv /sdcard/Download/
```

### Opción 4: Generar Programáticamente
Copia este código en la consola de Android Studio:

```kotlin
// Crear archivo de prueba en Download
val file = File("/sdcard/Download/inventario_test.csv")
file.writeText("""SKU,Descripcion,CxPal,FPC,Con,Centro,Linea,OP,FProd,DiasV,Ubicacion,TotalPallets,TipoTarima,Almacen
SKU001,Producto 1,12,100,50,CENTRO1,LINEA1,OP001,2024-01-01,5,Pasillo A-01,10,PLASTICO,ALMACEN1
SKU002,Producto 2,24,200,100,CENTRO1,LINEA2,OP002,2024-01-02,3,Pasillo B-01,5,MADERA,ALMACEN2""")
```

---

## Archivos de Prueba para Casos de Error

### CSV Vacío (Debe fallar gracefully)
**Nombre:** `vacio.csv`
```
(archivo completamente vacío)
```

### CSV Solo con Header (Debe fallar gracefully)
**Nombre:** `solo_header.csv`
```csv
SKU,Descripcion,Cantidad
```

### CSV Malformado (Debe fallar gracefully)
**Nombre:** `malformado.csv`
```
SKU001 Producto 1 12 100 50
SKU002 Producto 2 24 200 100
(sin delimitadores)
```

---

## Validación de Archivos

### Checklist de CSV Válido:
- [ ] Tiene headers en la primera fila
- [ ] Tiene al menos 1 fila de datos
- [ ] Cada fila tiene el mismo número de columnas que el header
- [ ] Las comillas están escapadas correctamente (" -> "")
- [ ] No hay espacios en blanco en los nombres de columnas
- [ ] Está guardado en UTF-8

### Columnas Requeridas por Tipo:

#### Inventario (INVENTARIO):
- SKU (obligatorio)
- Descripcion
- CxPal
- FPC
- Con
- Centro
- Linea
- OP
- FProd
- DiasV
- Ubicacion
- TotalPallets
- TipoTarima
- Almacen

#### Tarimas/Sistema (TARIMAS, SISTEMA):
- SKU (obligatorio)
- Descripcion
- Cantidad (o CajasXPallet)
- CajasXPallet
- Armado (opcional)
- TarimaPlasticaSku (opcional)

---

## Pruebas Recomendadas

### Test 1: Importación Exitosa
```
Usar: inventario_test.csv
Esperado: "Se importaron 4 registros"
```

### Test 2: Archivo Vacío
```
Usar: vacio.csv
Esperado: "El archivo está vacío"
```

### Test 3: Caracteres Especiales
```
Usar: inventario_especial.csv
Esperado: "Se importaron 4 registros"
```

### Test 4: Separador Diferente
```
Usar: tarimas_semicolon.csv
Esperado: "Se importaron 3 registros"
```

### Test 5: Archivo Corrompido
```
Usar: malformado.csv
Esperado: "Error: [mensaje descriptivo]"
```

---

## Notas

- Los archivos CSV deben estar en **UTF-8**
- Los delimitadores soportados son: `,` (coma) y `;` (punto y coma)
- Las comillas dobles dentro de campos deben escaparse: `"` → `""`
- Las líneas en blanco se ignoran automáticamente
- El sistema es tolerante con espacios en blanco (se trim automáticamente)

---

**Última actualización:** 15/04/2026

