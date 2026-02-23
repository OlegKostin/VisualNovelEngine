package com.olegkos.coredi

import com.olegkos.virtualnoveltesttwo.GameLoader
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import org.koin.dsl.module
import org.koin.core.context.startKoin

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
    modules(desktopModule)
  }
}