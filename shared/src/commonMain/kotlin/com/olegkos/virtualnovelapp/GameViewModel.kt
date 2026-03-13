package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olegkos.save.SaveManager
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.engine.NodePointer
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
import java.io.File

class GameViewModel(
  private val loader: GameLoader,
  private val dice: DiceRoller,
  private val saveManager: SaveManager,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
  private var currentScenario: String = "game/game.json"
  var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
    private set
  val currentNode: SceneNode?
    get() = engine?.currentNode()
  private var _assetsRoot: String = ""
  val assetsRoot: String get() = _assetsRoot
  private var engine: VnEngine? = null

  init { loadGame() }

  private fun loadGame() {
    viewModelScope.launch {
      val game = withContext(ioDispatcher) { loader.load("game/game.json") }
      _assetsRoot = game.assetsRoot
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
      viewModelScope.launch {
        currentScenario = output.scenarioFile
        val newScenario = withContext(ioDispatcher) { loader.load(output.scenarioFile) }
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
  fun saveGame(slot: String) {

    val engine = engine ?: return

    val node = engine.currentNode()

    saveManager.save(
      slot = slot,
      state = engine.state,
      scenario = currentScenario
    )
  }  fun loadSave(slot: String) {

    viewModelScope.launch {

      val state = saveManager.load(slot) ?: return@launch

      val scenario = withContext(ioDispatcher) {
        loader.load(currentScenario)
      }

      engine = VnEngine(state, dice).apply {
        addScenes(scenario.scenario.scenes)
      }

      currentOutput = engine?.step() ?: EngineOutput.Loading
    }
  }
  fun listSaves(): List<String> =
    saveManager.listSaves()
}