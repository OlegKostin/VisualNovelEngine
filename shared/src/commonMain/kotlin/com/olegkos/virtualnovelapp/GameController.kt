package com.olegkos.virtualnovelapp

import com.olegkos.save.SaveManager
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioParser
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class GameController(
  private val loader: GameLoader,
  private val parser: ScenarioParser,
  private val dice: DiceRoller,
  private val assetReader: AssetReader,
  private val saveManager: SaveManager,
  private val ioDispatcher: CoroutineDispatcher
) {

  private val basePath = "game/"
  private val gameConfigPath = basePath +"game.json"

  private var engine: VnEngine? = null
  val requireEngine: VnEngine
    get() = engine ?: error("Engine not initialized")
  private var currentScenario: String = ""

  lateinit var assets: AssetPathResolver
    private set

  lateinit var reader: AssetReader
    private set

  suspend fun init(): Pair<EngineOutput, SceneNode?> {
    val game = withContext(ioDispatcher) {
      loader.load(gameConfigPath)
    }

    this.assets = game.assets
    this.reader = assetReader

    currentScenario = game.scenarioPath

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

        value.jsonPrimitive.floatOrNull != null ->
          GameValue.FloatVal(value.jsonPrimitive.float)

        value.jsonPrimitive.intOrNull != null ->
          GameValue.IntVal(value.jsonPrimitive.int)

        else -> GameValue.IntVal(0)
      }
    }

    engine = VnEngine(state, dice).apply {
      addScenes(game.scenario.scenes)
    }

    return step()
  }

  fun next(option: Option? = null): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    val output = engine.step(option)

    if (output is EngineOutput.JumpScenarioOutput) {
      return output to null
    }

    if (output is EngineOutput.EndOfScene) {
      return output to null
    }

    return output to engine.currentNode()
  }

  suspend fun switchScenario(path: String): Pair<EngineOutput, SceneNode?> {
    val scenario = loadScenario(path)

    engine?.addScenes(scenario.scenes)
    engine?.state?.pointer = NodePointer(scenario.startSceneId, 0)

    currentScenario = path

    return step()
  }

  fun rollDice(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    val node = engine.currentNode() as? SceneNode.DiceRoll ?: return step()

    engine.state.diceResult = engine.dice.roll(node.sides)

    return step()
  }

  fun saveGame(slot: String) {
    val engine = engine ?: return

    saveManager.save(
      slot = slot,
      state = engine.state,
      scenario = currentScenario
    )
  }

  suspend fun loadSave(slot: String): Pair<EngineOutput, SceneNode?> {
    val loaded = saveManager.load(slot) ?: return EngineOutput.Loading to null

    val scenarioPath = loaded.scenario

    if (!scenarioPath.endsWith(".json")) {
      throw IllegalStateException("Corrupted save: scenario=$scenarioPath")
    }

    currentScenario = scenarioPath

    val scenario = loadScenario(currentScenario)

    engine = VnEngine(loaded.state, dice).apply {
      addScenes(scenario.scenes)
    }

    return step()
  }

  private suspend fun loadScenario(path: String) =
    withContext(ioDispatcher) {
      val raw = assetReader.readText(path)
      parser.parse(raw)
    }

  private fun step(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    return engine.step() to engine.currentNode()
  }

  fun listSaves(): List<String> =
    saveManager.listSaves()
}