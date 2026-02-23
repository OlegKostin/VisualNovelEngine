package com.olegkos.coredi

import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.GameLoader
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule: Module = module {
  single { GameLoader() }
  single {
    val loader: GameLoader = get()
    val engine = VnEngine(GameState("intro"))
    engine.addScenes(loader.load())
    engine
  }
  viewModelOf(::GameViewModel)
}
