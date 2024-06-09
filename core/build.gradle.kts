import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

// See https://stackoverflow.com/a/77033030/1401879
tasks.withType<JavaExec> {
    standardInput = System.`in`
}

kotlin {
    targetHierarchy.default()
    jvm().mainRun {
        mainClass = "org.kobjects.basik.MainKt"
    }
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // linuxX64() TODO

    /*
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
     */

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.kobjects.parsek:core:0.8.0")
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
