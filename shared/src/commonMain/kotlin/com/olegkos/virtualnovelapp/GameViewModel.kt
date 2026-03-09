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
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class GameViewModel(
  private val loader: GameLoader,
  private val dice: DiceRoller,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

  var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
    private set

  private var engine: VnEngine? = null

  init { loadGame() }

  private fun loadGame() {
    viewModelScope.launch {
      val game = withContext(ioDispatcher) { loader.load("game/game.json") }
      val varsRaw = loader.assets.readText("game/variables/variables.json")
      val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
      val varsMap = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(varsRaw)
      val state = GameState(NodePointer(game.scenario.startSceneId, 0))

      varsMap.forEach { (key, value) ->
        state.variables[key] = when {
          value.jsonPrimitive.isString -> GameValue.StringVal(value.jsonPrimitive.content)
          value.jsonPrimitive.booleanOrNull != null -> GameValue.Bool(value.jsonPrimitive.boolean)
          value.jsonPrimitive.intOrNull != null -> GameValue.IntVal(value.jsonPrimitive.int)
          value.jsonPrimitive.floatOrNull != null -> GameValue.FloatVal(value.jsonPrimitive.float)
          else -> GameValue.IntVal(0)
        }
      }

      engine = VnEngine(state, dice).apply { addScenes(game.scenario.scenes) }
      currentOutput = engine?.step() ?: EngineOutput.Loading
    }
  }

  fun next(option: Option? = null) {
    val engine = engine ?: return

    val output = engine.step(option)

    if (output is EngineOutput.JumpScenarioOutput) {
      viewModelScope.launch(ioDispatcher) {
        val newScenario = loader.load(output.scenarioFile)
        engine.addScenes(newScenario.scenario.scenes)
        engine.state.pointer = NodePointer(newScenario.scenario.startSceneId, 0)

        currentOutput = engine.step()
      }
    } else {
      currentOutput = output
    }
  }

  fun rollDice() {
    engine?.state?.diceResult = engine?.dice?.roll(engine!!.currentNode().let { it as SceneNode.DiceRoll }.sides)
    currentOutput = engine?.step() ?: EngineOutput.Loading
  }
}