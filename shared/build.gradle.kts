import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.androidKmpLibrary)
}

kotlin {
  androidLibrary {
    namespace = "com.olegkos.shared"
    compileSdk = 36
    minSdk = 26

  }

  jvm()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.vnEngine)
        implementation(libs.runtime)
        implementation(libs.foundation)
        implementation(libs.material3)
        implementation(libs.ui)
        implementation(libs.components.resources)
        implementation(libs.ui.tooling.preview)
        implementation(libs.androidx.lifecycle.viewmodelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)

        api(libs.koin.core)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.viewmodel)
        implementation(libs.lifecycle.viewmodel)
        implementation(libs.navigation.compose)
      }
    }

    val androidMain by getting {
      dependencies{
        implementation(libs.koin.android)
      }
    }

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