package com.olegkos.coredi

import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.GameLoading.DevScenarioProvider
import com.olegkos.virtualnoveltesttwo.GameLoading.DiceRoller
import com.olegkos.virtualnoveltesttwo.GameLoading.NodePointer
import com.olegkos.virtualnoveltesttwo.GameLoading.RandomDiceRoller
import com.olegkos.virtualnoveltesttwo.GameLoading.ScenarioProvider
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule: Module = module {

  // Провайдер сценария — можно легко подменять
  single<ScenarioProvider> { DevScenarioProvider() }

  // Кубик
  single<DiceRoller> { RandomDiceRoller() }

  // Движок VN
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