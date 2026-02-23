package com.olegkos.virtualnoveltesttwo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.olegkos.virtualnovelapp.App
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.GameLoader
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel

val desktopModule = module {
    single { GameLoader() }
    single {
        val loader: GameLoader = get()
        val engine = VnEngine(GameState("intro"))
        engine.addScenes(loader.load())
        engine
    }
}

fun initKoinDesktop() {
    startKoin {
        modules(desktopModule,shareModule, androidModule)
    }
}
fun main() = application {
    initKoinDesktop()

    Window(onCloseRequest = ::exitApplication, title = "VN Test") {
        App()
    }
}


val shareModule = module {
    single { GameLoader() }
    single {
        val loader: GameLoader = get()
        val engine = VnEngine(GameState("intro"))
        engine.addScenes(loader.load())
        engine
    }
}
val androidModule = module {
    viewModel { GameViewModel(get()) }
}