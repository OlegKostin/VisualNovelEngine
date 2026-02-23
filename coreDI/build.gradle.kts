plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKmpLibrary)
}

kotlin {

  // Target declarations - add or remove as needed below. These define
  // which platforms this KMP module supports.
  // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
  androidLibrary {
    namespace = "com.olegkos.coreDI"
    compileSdk = 36
    minSdk = 26
  }
  jvm()
  // Source set declarations.
  // Declaring a target automatically creates a source set with the same name. By default, the
  // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
  // common to share sources between related targets.
  // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.koin.core)
        implementation(projects.vnEngine)
        implementation(projects.shared)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.viewmodel)
        implementation(libs.lifecycle.viewmodel)
        implementation(libs.navigation.compose)
        // Add KMP dependencies here
      }
    }


    androidMain {
      dependencies {
        implementation(libs.koin.android)
        implementation(libs.koin.androidx.compose)
        // Add Android-specific dependencies here. Note that this source set depends on
        // commonMain by default and will correctly pull the Android artifacts of any KMP
        // dependencies declared in commonMain.
      }
    }

  }

}