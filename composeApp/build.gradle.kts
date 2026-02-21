import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    androidLibrary {
        namespace = "com.olegkos.virtualnoveltesttwo"
        compileSdk = 36
        minSdk = 26

    }

    jvm() // Desktop target

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":vnEngine"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.preview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }

        val androidMain by getting

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.olegkos.virtualnoveltesttwo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.olegkos.virtualnoveltesttwo"
            packageVersion = "1.0.0"
        }
    }
}