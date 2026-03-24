package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olegkos.save.SaveManager
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.NodePointer
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class GameViewModel(
  private val loader: GameLoader,
  private val dice: DiceRoller,
  private val assetReader: AssetReader,
  private val saveManager: SaveManager,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
  val basePath = "game/"
  private var currentScenario: String = basePath+"game.json"
  val reader get() = assetReader
  private var _assets: AssetPathResolver? = null
  val assets get() = _assets!!

  private var _assetsRoot: String = ""

  var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
    private set

  var currentNode by mutableStateOf<SceneNode?>(null)
    private set

  private var engine: VnEngine? = null

  init {
    loadGame()
  }

  private fun loadGame() {
    viewModelScope.launch {

      val game = withContext(ioDispatcher) {
        loader.load(basePath+"game.json")
      }

      _assetsRoot = game.assetsRoot
      _assets = game.assets


      val varsRaw = assetReader.readText(basePath + game.variables)

      val json = Json { ignoreUnknownKeys = true }
      val varsMap = json.decodeFromString<Map<String, JsonElement>>(varsRaw)

      val state = GameState(NodePointer(game.scenario.startSceneId, 0))

      varsMap.forEach { (key, value) ->
        state.variables[key] = when {
          value.jsonPrimitive.isString ->
            GameValue.StringVal(value.jsonPrimitive.content)

          value.jsonPrimitive.booleanOrNull != null ->
            GameValue.Bool(value.jsonPrimitive.boolean)

          value.jsonPrimitive.intOrNull != null ->
            GameValue.IntVal(value.jsonPrimitive.int)

          value.jsonPrimitive.floatOrNull != null ->
            GameValue.FloatVal(value.jsonPrimitive.float)

          else -> GameValue.IntVal(0)
        }
      }
      engine = VnEngine(state, dice).apply {
        addScenes(game.scenario.scenes)
      }

      currentOutput = engine!!.step()
      currentNode = engine!!.currentNode()
    }
  }

  fun next(option: Option? = null) {
    val engine = engine ?: return

    val output = engine.step(option)

    currentNode = engine.currentNode()

    if (output is EngineOutput.JumpScenarioOutput) {
      viewModelScope.launch {

        currentScenario = output.scenarioFile

        val newScenario = withContext(ioDispatcher) {
          loader.load(output.scenarioFile)
        }

        engine.addScenes(newScenario.scenario.scenes)
        engine.state.pointer = NodePointer(newScenario.scenario.startSceneId, 0)

        currentOutput = engine.step()
        currentNode = engine.currentNode()
      }
    } else {
      currentOutput = output
    }
  }

  fun rollDice() {
    val engine = engine ?: return
    val node = engine.currentNode() as? SceneNode.DiceRoll ?: return

    engine.state.diceResult = engine.dice.roll(node.sides)

    currentOutput = engine.step()
    currentNode = engine.currentNode()
  }

  fun saveGame(slot: String) {
    val engine = engine ?: return

    saveManager.save(
      slot = slot,
      state = engine.state,
      scenario = currentScenario
    )
  }

  fun loadSave(slot: String) {
    viewModelScope.launch {

      val loaded = saveManager.load(slot) ?: return@launch

      currentScenario = loaded.scenario

      val newScenario = withContext(ioDispatcher) {
        loader.load(currentScenario)
      }

      engine = VnEngine(loaded.state, dice).apply {
        addScenes(newScenario.scenario.scenes)
      }

      currentOutput = engine!!.step()
      currentNode = engine!!.currentNode()
    }
  }
  
  fun listSaves(): List<String> =
    saveManager.listSaves()
}