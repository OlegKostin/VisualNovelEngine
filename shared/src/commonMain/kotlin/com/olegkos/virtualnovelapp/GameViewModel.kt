package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olegkos.virtualnoveltesttwo.GameLoading.DiceRoller
import com.olegkos.virtualnoveltesttwo.GameLoading.NodePointer
import com.olegkos.virtualnoveltesttwo.GameLoading.ScenarioProvider
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.scene.Option
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
  private val provider: ScenarioProvider,
  private val dice: DiceRoller,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

  // currentOutput никогда не null, изначально Loading
  var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
    private set

  private var engine: VnEngine? = null

  init {
    // Инициализация движка асинхронно
    viewModelScope.launch {
      val scenario = withContext(ioDispatcher) { provider.load() }

      engine = VnEngine(
        state = GameState(NodePointer(scenario.startSceneId, 0)),
        dice = dice
      ).apply { addScenes(scenario.scenes) }

      // После инициализации сразу обновляем вывод
      currentOutput = engine!!.currentOutput()
    }
  }

  fun next(option: Option? = null) {
    engine?.next(option)
    // Обновляем состояние после шага
    currentOutput = engine?.currentOutput() ?: EngineOutput.Loading
  }

  fun rollDice() {
    next()
  }
}