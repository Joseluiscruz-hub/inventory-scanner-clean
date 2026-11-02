import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("jacoco") // Jacoco para cobertura
    alias(libs.plugins.hilt.android)
}

// Permite compilar y ejecutar pruebas desde un checkout limpio. Firebase solo se
// configura cuando el archivo del proyecto se proporciona fuera del control de versiones.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn("google-services.json no está presente; se omite la configuración de Firebase")
}

// Release signing: keystore.properties (gitignored) or ISP_* env vars. No secrets in git.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}
fun signingProp(key: String, env: String): String? =
    keystoreProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: System.getenv(env)?.takeIf { it.isNotBlank() }

android {
    namespace = "com.joseluiscruz.capturainventario"
    compileSdk = 35

    lint {
        abortOnError = true
    }

    defaultConfig {
        applicationId = "com.joseluiscruz.capturainventario"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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
            } else {
                logger.warn(
                    "Release signing incomplete — copy keystore.properties.example to " +
                        "keystore.properties or set ISP_STORE_FILE / ISP_STORE_PASSWORD / " +
                        "ISP_KEY_ALIAS / ISP_KEY_PASSWORD."
                )
            }
        }
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    testOptions {
        unitTests {
            // Recursos Android disponibles en tests JVM (Robolectric: arnés de capturas de pantalla).
            isIncludeAndroidResources = true
            all { test ->
                // Arnés de capturas (CapturasPantallaTest): solo corre si se pide explícitamente con
                //   ./gradlew :app:testDebugUnitTest --tests '*CapturasPantallaTest*' \
                //       -PcapturasDir=/ruta/salida -PcapturasVariante=antes|despues
                // En CI y en `testDebugUnitTest` normal se excluye (no descarga el runtime de Robolectric).
                val capturasDir = project.findProperty("capturasDir")?.toString()
                if (capturasDir == null) {
                    test.exclude("**/capturas/**")
                } else {
                    test.systemProperty("capturas.out", capturasDir)
                    test.systemProperty(
                        "capturas.variante",
                        project.findProperty("capturasVariante")?.toString() ?: "actual"
                    )
                }
            }
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    ksp {
        // Export Room schema files so migrations can be verified in CI
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.withType<Test> {
    // useJUnitPlatform() // Removed to support JUnit 4
    finalizedBy("jacocoTestReport")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        // Excluir clases generadas y de Android
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(fileFilter)
        },
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude(fileFilter)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"))
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.gridlayout)

    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.materialIconsExtended)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.bundles.navigation)

    // ZXing and JourneyApps barcode scanning
    implementation(libs.zxing.embedded)
    implementation(libs.zxing.core)
    implementation(libs.play.services.code.scanner)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.byte.buddy)
    testImplementation(libs.byte.buddy.agent)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    // MPAndroidChart
    implementation(libs.github.mpandroidchart)

    // Vico Charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Lottie for animations
    implementation(libs.airbnb.lottie)
    implementation(libs.airbnb.lottie.compose)

    // Gson for JSON parsing
    implementation(libs.google.gson)

    // Retrofit for network calls
    implementation(libs.bundles.retrofit)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    debugImplementation("com.google.firebase:firebase-appcheck-debug")
    releaseImplementation("com.google.firebase:firebase-appcheck-playintegrity")

    // SwipeRefreshLayout
    implementation(libs.androidx.swiperefreshlayout)
}
