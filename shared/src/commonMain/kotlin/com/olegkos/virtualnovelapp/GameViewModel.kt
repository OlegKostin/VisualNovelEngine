package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.NodePointer
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.scene.Option
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
  private val loader: GameLoader,
  private val dice: DiceRoller,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

  var currentOutput by mutableStateOf<EngineOutput>(
    EngineOutput.Loading
  )
    private set

  private var engine: VnEngine? = null

  init {
    loadGame()
  }

  private fun loadGame() {
    viewModelScope.launch {

      val game = withContext(ioDispatcher) {
        loader.load("game/game.json")
      }

      engine = VnEngine(
        state = GameState(
          NodePointer(
            game.scenario.startSceneId,
            0
          )
        ),
        dice = dice
      ).apply {
        addScenes(game.scenario.scenes)
      }

      currentOutput =
        engine!!.currentOutput()
    }
  }

  fun next(option: Option? = null) {
    engine?.next(option)
    currentOutput =
      engine?.currentOutput()
        ?: EngineOutput.Loading
  }

  fun rollDice() {
    next()
  }
}