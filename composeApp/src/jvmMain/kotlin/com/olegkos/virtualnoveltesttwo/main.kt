package com.olegkos.virtualnoveltesttwo

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.olegkos.coredi.platformModule
import com.olegkos.virtualnoveltesttwo.theme.VnAppTheme
import org.koin.core.context.startKoin

fun initKoinDesktop() {
  startKoin {
    modules(platformModule)
  }
}

fun main() = application {
  initKoinDesktop()

  Window(onCloseRequest = ::exitApplication, title = "VN Test") {
    DisposableEffect(window) {
      RegisteredComposeWindow.window = window
      onDispose {
        if (RegisteredComposeWindow.window === window) {
          RegisteredComposeWindow.window = null
        }
      }
    }
    VnAppTheme {
      App()
    }
  }
}
