# README de Entornos (Windows + Linux/Contenedor)

Este documento define cómo trabajar el proyecto en ambos entornos sin romper la compilación ni ensuciar el estado de Git.

## Objetivo

Mantener una sola rama de trabajo para código fuente y separar configuraciones locales por entorno.

## Principios clave

1. `local.properties` es local de cada máquina y no debe subirse al repositorio.
2. El código de la app debe permanecer igual en ambos entornos.
3. Las diferencias de SDK/JDK se resuelven con rutas locales y variables de entorno.
4. Si se requiere CI/CD, usar variables de entorno y no rutas hardcodeadas de una máquina personal.

## Configuración por entorno

### Windows (IntelliJ IDEA / Android Studio)

Ejemplo de `local.properties`:

sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

Variables recomendadas en terminal (PowerShell):

$env:ANDROID_SDK_ROOT="$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_HOME=$env:ANDROID_SDK_ROOT

### Linux / Dev Container

Ejemplo de `local.properties`:

sdk.dir=/home/codespace/android-sdk

Variables recomendadas en terminal:

export ANDROID_SDK_ROOT=/home/codespace/android-sdk
export ANDROID_HOME=$ANDROID_SDK_ROOT
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

## Manejo de local.properties para evitar ruido en Git

En este repositorio, `local.properties` debe quedar **sin trackear** y en `.gitignore`.

Si el archivo `local.properties` ya está siendo trackeado por Git, lo ideal es eliminarlo del índice (manteniendo el archivo físico) y asegurar que esté en `.gitignore`:

```sh
git rm --cached local.properties
```

De esta manera, no es necesario usar flags como `skip-worktree` y se mantiene la limpieza del repositorio para todos los usuarios.

Solo como medida temporal (si aún no puedes des-trackearlo), puedes marcar cambios locales:

```sh
# Marca local.properties para ignorar cambios locales (solo en tu clon)
git update-index --skip-worktree local.properties
# Verifica el estado: prefijo 'S' indica skip-worktree activo
git ls-files -v | grep local.properties
```

Si aparece con prefijo `S`, está marcado como `skip-worktree`.

## Flujo recomendado de trabajo diario

1. Abrir proyecto en IntelliJ IDEA o Android Studio.
2. Verificar JDK (17 o superior para Gradle 9).
3. Verificar ruta SDK del entorno actual en `local.properties`.
4. Sincronizar Gradle.
5. Compilar `assembleDebug`.
6. Confirmar que `git status` no muestra cambios locales no deseados.

## Comandos útiles de build

Linux:

./gradlew clean assembleDebug --no-daemon --console=plain

Windows:

gradlew.bat clean assembleDebug --no-daemon --console=plain

## Salida esperada del APK debug

app/build/outputs/apk/debug/app-debug.apk

## Troubleshooting rápido

### Error: Gradle requires JVM 17 or later

Causa: Java 11 activo.

Accion:

- Linux: exportar `JAVA_HOME` a JDK 17.
- Windows: seleccionar JDK 17 en IDE o terminal.

### Error: SDK location not found

Causa: `sdk.dir` invalido para el entorno actual.

Accion:

- Corregir `local.properties` con la ruta del SDK local.
- Alternativamente definir `ANDROID_HOME` y `ANDROID_SDK_ROOT`.

### Build pasa en un entorno y falla en otro

Revisar:

1. Version de JDK.
2. Versiones de Android SDK/platform/build-tools.
3. Variables de entorno activas.
4. Que no se haya subido un `local.properties` especifico de una maquina.

## Recomendaciones para futuras actualizaciones

1. No mezclar cambios de infraestructura local con cambios funcionales de app en el mismo commit.
2. Si necesitas ajustar scripts de build, documentar el motivo y el entorno en este archivo.
3. Mantener actualizado este documento cuando cambie: Gradle, JDK minimo, compileSdk o ruta estandar del SDK en el equipo.
4. Si se agrega CI, declarar JDK y Android SDK en el workflow para reproducibilidad.

## Nota sobre ramas

No es necesario crear otra rama solo para alternar Windows/Linux.

Si conviene crear otra rama cuando:

- Hagas cambios funcionales grandes.
- Realices migraciones de version (AGP, Gradle, Kotlin).
- Introduzcas cambios de pipeline (CI/CD).
