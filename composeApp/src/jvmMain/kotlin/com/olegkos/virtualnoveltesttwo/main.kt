package com.olegkos.virtualnoveltesttwo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import com.olegkos.coredi.platformModule

fun initKoinDesktop() {
    startKoin {
        modules(platformModule)
    }
}
fun main() = application {
    initKoinDesktop()

    Window(onCloseRequest = ::exitApplication, title = "VN Test") {
        App()
    }
}


