import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

// See https://stackoverflow.com/a/77033030/1401879
tasks.withType<JavaExec> {
    standardInput = System.`in`
}

kotlin {
    applyDefaultHierarchyTemplate()
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    jvm().mainRun {
        mainClass = "org.kobjects.basik.MainKt"
    }
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // linuxX64() TODO
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        //     moduleName = "konsole"
        browser {
            /*       commonWebpackConfig {
                       outputFileName = "konsole.js"
                       devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                           static = (static ?: mutableListOf()).apply {
                               // Serve sources to debug inside browser
                               add(project.projectDir.path)
                           }
                       }
                   }*/
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.parsek.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "org.kobjects.basik"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
