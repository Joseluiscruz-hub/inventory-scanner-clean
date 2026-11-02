# Validación de la migración Room 9 → 10 con una base real

La prueba automatizada `AppDatabaseMigrationTest` valida la estructura y los datos con una base sintética. Antes de liberar a producción también se debe ejecutar `AppDatabaseRealMigrationTest` sobre una copia representativa de la base de un dispositivo que todavía use el esquema 9.

## Precauciones

- Usa un dispositivo de laboratorio o una copia autorizada y anonimizada.
- No adjuntes ni subas la base a GitHub, CI, tickets o mensajería. El directorio local está ignorado por Git.
- Conserva el respaldo original fuera del proyecto y trabaja siempre sobre una copia.
- Si existen archivos `-wal` o `-shm`, fuerza primero un checkpoint de SQLite o copia los tres archivos de forma consistente. Una copia incompleta puede parecer corrupta aunque la migración sea correcta.

## Preparar la copia local

1. Cierra la aplicación y genera una copia consistente de `inventory_database` en un equipo autorizado.
2. Si se trata de una compilación depurable en un dispositivo de laboratorio, se puede extraer con:

   ```bash
   adb exec-out run-as com.joseluiscruz.capturainventario \
     cat databases/inventory_database > inventory_database_v9.db
   ```

   Una compilación de producción normalmente no permite `run-as`; en ese caso usa el procedimiento de respaldo aprobado para el dispositivo, sin debilitar la aplicación ni sus controles.

3. Verifica localmente que `PRAGMA user_version` sea `9` y anonimiza cualquier dato que no sea necesario para probar la migración.
4. Copia el archivo a:

   ```text
   app/src/androidTest/assets/real-db/inventory_database_v9.db
   ```

## Ejecutar y comprobar

Con un emulador o dispositivo conectado, ejecuta solamente esta prueba:

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.joseluiscruz.capturainventario.data.AppDatabaseRealMigrationTest
```

La prueba comprueba que:

- la copia de entrada tiene versión 9;
- Room aplica exactamente `MIGRATION_9_10`;
- la base resultante queda en versión 10;
- no cambia el total de eventos de inventario;
- existen las columnas nuevas `cantidadRestos` y `tipoTarima`.

Después de la ejecución, elimina la copia de trabajo del directorio `assets/real-db`. Antes de preparar el release, confirma con `git status` que ningún archivo `.db`, `.db-wal` o `.db-shm` esté incluido.

## Evidencia mínima de liberación

Registra en el checklist privado del release: modelo y versión Android del dispositivo de origen, versión de la app, cantidad de filas antes/después, resultado de la prueba y fecha. No registres contenido del inventario ni adjuntes la base.
