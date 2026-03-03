package com.olegkos.coredi

import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.vnengine.GameLoading.DevScenarioProvider
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.NodePointer
import com.olegkos.vnengine.GameLoading.RandomDiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioProvider
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule: Module = module {

  single<ScenarioProvider> { DevScenarioProvider() }


  single<DiceRoller> { RandomDiceRoller() }


  single {
    val provider: ScenarioProvider = get()
    val dice: DiceRoller = get()

    val scenario = runBlocking { provider.load() }  // ⚡ тут безопасно для Dev

    val engine = VnEngine(
      state = GameState(pointer = NodePointer(scenario.startSceneId, 0)),
      dice = dice
    )

    engine.addScenes(scenario.scenes)
    engine
  }
  // ViewModel
  viewModelOf(::GameViewModel)
}