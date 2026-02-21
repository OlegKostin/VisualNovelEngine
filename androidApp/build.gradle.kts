plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeCompiler)
}

android {
  namespace = "com.olegkos.androidapp"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.olegkos.androidapp"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(project(":composeApp")) // KMP library

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.activity.compose)

  implementation(libs.compose.ui)
  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation(libs.compose.runtime)
  implementation(libs.compose.uiToolingPreview)

  debugImplementation(libs.compose.uiTooling)
}