# Release signingConfigs snippet (app/build.gradle.kts)

Add near top (after plugins / google-services conditional):

```kotlin
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}
fun signingProp(key: String, env: String): String? =
    keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: System.getenv(env)?.takeIf { it.isNotBlank() }
```

Inside `android {` before `buildTypes`:

```kotlin
    signingConfigs {
        create("release") {
            val storePath = signingProp("storeFile", "ISP_STORE_FILE")
            val storePass = signingProp("storePassword", "ISP_STORE_PASSWORD")
            val alias = signingProp("keyAlias", "ISP_KEY_ALIAS")
            val keyPass = signingProp("keyPassword", "ISP_KEY_PASSWORD")
            if (storePath != null && storePass != null && alias != null && keyPass != null) {
                storeFile = rootProject.file(storePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            }
        }
    }
```

In `buildTypes.release` keep minify/shrink and wire:

```kotlin
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
```

Dependency: `implementation(libs.firebase.functions)` and catalog alias
`firebase-functions = { group = "com.google.firebase", name = "firebase-functions" }`.
