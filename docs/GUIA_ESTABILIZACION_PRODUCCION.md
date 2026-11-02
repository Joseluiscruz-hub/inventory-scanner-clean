# Guía de estabilización para producción

Esta guía prioriza los cambios necesarios para llevar InventoryScannerPro de piloto avanzado a una aplicación operativa confiable. Los pasos están ordenados por riesgo, no por facilidad.

## Fase 1 — Bloqueadores de entrega

Estado: implementado en el árbol de trabajo actual.

- Inicializar `AuthManager` desde `Application` para soportar restauración de Activities después de que Android finalice el proceso.
- Mantener Firebase opcional en builds de CI sin `google-services.json`.
- Corregir los errores de lint `MissingDefaultResource`, `NewApi` y `StringFormatInvalid`.
- Activar `abortOnError` para impedir que nuevos errores críticos produzcan un build aparentemente válido.
- Mostrar siempre `BuildConfig.VERSION_NAME` en la interfaz.
- Generar cobertura JaCoCo a partir de las clases Kotlin y Java reales.
- Hacer que CI falle si no puede generar la cobertura.

Validación obligatoria:

```bash
./gradlew testDebugUnitTest jacocoTestReport lintDebug assembleDebug assembleRelease
```

## Fase 2 — Pruebas de operación

Objetivo: cubrir los fallos que una prueba unitaria no puede detectar.

1. Ejecutar las pruebas instrumentadas en API 24, una versión intermedia y API 35.
2. Añadir una prueba de restauración del proceso: iniciar sesión, abrir la pantalla principal, recrear el proceso y confirmar que el rol y la navegación no fallan.
3. Probar la migración Room 9→10 con una copia anonimizada de una base real.
4. Probar importaciones CSV grandes, filas dañadas, duplicados, caracteres especiales y archivos sin permisos persistentes.
5. Simular pérdida de red durante escaneo, sincronización y traspaso; verificar que no existan duplicados ni pérdida de movimientos.
6. Validar `firestore.rules` en Firebase Emulator para roles sin sesión, OPERADOR y ADMIN.

Criterio de salida: cero fallos en los flujos de captura, importación, comparación, reinicio, traspaso y recuperación sin red.

## Fase 3 — Reducir deuda técnica

Objetivo: bajar el costo de cada cambio futuro.

1. Dividir `InventarioScreen`, `InventarioComposeViewModel` e `InventarioResumenComposeActivity` en componentes y casos de uso de menos de 300–400 líneas.
2. Identificar pantallas realmente alcanzables desde el `AndroidManifest` y la navegación antes de retirar Activities, layouts y drawables heredados.
3. Eliminar recursos sin uso en lotes pequeños, ejecutando lint y una prueba visual después de cada lote.
4. Mover textos visibles a recursos `strings.xml`; priorizar mensajes operativos, errores y accesibilidad.
5. Sustituir `printStackTrace` y logs con datos operativos por una capa de registro que oculte SKU, usuario, rutas y contenido importado en release.
6. Resolver gradualmente APIs obsoletas y recomendaciones de dependencias reportadas por lint.

Meta recomendada: ninguna clase de interfaz o ViewModel por encima de 500 líneas y cero warnings nuevos respecto a la línea base.

## Fase 4 — Entrega controlada

1. Administrar la clave de firma fuera del repositorio y entregar el AAB/APK firmado desde un entorno protegido.
2. Desplegar y probar reglas de Firestore en staging antes de producción.
3. Habilitar App Check de forma gradual y confirmar que ningún dispositivo operativo legítimo quede bloqueado.
4. Realizar un piloto con pocos operadores y conservar un procedimiento documentado de reversión.
5. Medir fallos, tiempo por inventario, reintentos de sincronización y discrepancias antes de ampliar el despliegue.

## Definición de “lista para producción”

- Compilación debug y release correctas con lint bloqueante.
- Pruebas unitarias e instrumentadas verdes.
- Cobertura publicada y con umbral acordado para lógica de dominio.
- Migración Room verificada con datos representativos.
- Reglas, roles y App Check probados en staging.
- Paquete firmado, versionado desde una única fuente y con plan de reversión.
- Sin pérdida ni duplicación de inventario en pruebas de red inestable.
