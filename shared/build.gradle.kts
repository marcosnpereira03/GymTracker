import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun getSecret(key: String, defaultValue: String = ""): String {
    return System.getenv(key)
        ?: localProps.getProperty(key)
        ?: defaultValue
}

val generatedConfigDir = layout.buildDirectory.dir("generated/config/commonMain/kotlin")

val generateAppConfig = tasks.register("generateAppConfig") {
    val outputDir = generatedConfigDir.get().asFile
    outputs.dir(outputDir)
    inputs.property("geminiKey", getSecret("GEMINI_API_KEY", ""))
    inputs.property("supabaseUrl", getSecret("SUPABASE_URL", ""))
    inputs.property("supabaseAnonKey", getSecret("SUPABASE_ANON_KEY", ""))
    doLast {
        val geminiKey = getSecret("GEMINI_API_KEY", "")
        val supabaseUrl = getSecret("SUPABASE_URL", "")
        val supabaseAnonKey = getSecret("SUPABASE_ANON_KEY", "")

        val targetFile = File(outputDir, "org/marcosnpereira03/gymtracker/config/AppConfig.kt")
        targetFile.parentFile.mkdirs()
        targetFile.writeText(
            """
            package org.marcosnpereira03.gymtracker.config

            /**
             * Configuración generada automáticamente en tiempo de compilación desde variables de entorno / local.properties.
             * No modificar manualmente ni incluir secretos en el control de versiones.
             */
            object AppConfig {
                const val GEMINI_API_KEY: String = "$geminiKey"
                const val SUPABASE_URL: String = "$supabaseUrl"
                const val SUPABASE_ANON_KEY: String = "$supabaseAnonKey"
            }
            """.trimIndent()
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedConfigDir)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Lifecycle, ViewModel & Navigation
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.runtime.compose)
                implementation(libs.navigation.compose)

                // Coroutines, Serialization & DateTime
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Supabase & Ktor Core
                implementation(project.dependencies.platform(libs.supabase.bom))
                implementation(libs.supabase.postgrest)
                implementation(libs.supabase.auth)
                implementation(libs.supabase.storage)
                implementation(libs.ktor.client.core)


                // Koin (Inyección de Dependencias)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)

            // Motor de red para Android
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            // Motor de red para iOS
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppConfig)
}

android {
    namespace = "org.marcosnpereira03.gymtracker.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}