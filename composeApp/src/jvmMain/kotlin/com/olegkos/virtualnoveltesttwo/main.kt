package com.olegkos.virtualnoveltesttwo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.olegkos.virtualnovelapp.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "VN Test") {
        App()
    }
}