package com.olegkos.virtualnovelapp

import com.olegkos.save.SaveManager
import com.olegkos.save.metaStorage.MetaManager
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioParser
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.NodePointer
import com.olegkos.vnengine.engine.UiCard
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.engine.cards.CardConfig
import com.olegkos.vnengine.engine.cards.CardManager
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class GameController(
  private val loader: GameLoader,
  private val parser: ScenarioParser,
  private val dice: DiceRoller,
  private val assetReader: AssetReader,
  private val saveManager: SaveManager,
  private val metaManager: MetaManager,
  private val cardManager: CardManager,
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
    println("ENGINE INSTANCE: ${System.identityHashCode(engine)}")

    val game = withContext(ioDispatcher) {
      loader.load(gameConfigPath)
    }

    // assets
    this.assets = game.assets
    this.reader = assetReader
    currentScenario = game.scenarioPath

    val json = Json { ignoreUnknownKeys = true }

    // =========================
    // VARIABLES
    // =========================
    val varsRaw = assetReader.readText(basePath + game.variables)
    val varsMap = json.decodeFromString<Map<String, JsonElement>>(varsRaw)

    val state = GameState(
      pointer = NodePointer(game.scenario.startSceneId, 0)
    )

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

    // =========================
    // CARDS
    // =========================
    println("LOAD CARDS FROM: ${game.cards}")

    val cardsRaw = assetReader.readText(basePath + game.cards)

    val cardsList = try {
      val wrapper = json.decodeFromString<CardConfig>(cardsRaw)
      wrapper.cards
    } catch (e: Exception) {
      println("❌ ERROR PARSING CARDS: ${e.message}")
      emptyList()
    }
    println("CARDS LOADED: ${cardsList.size}")

    cardManager.setCards(cardsList)

    // =========================
    // ENGINE
    // =========================
    engine = VnEngine(state, dice).apply {
      addScenes(game.scenario.scenes)
    }

    // =========================
    // START GAME
    // =========================
    return step()
  }
  fun next(option: Option? = null): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    val output = engine.step(option)
    if (output is EngineOutput.ShowDice) {
      val cards = getPlayerCards()
      return output.copy(cards = cards) to engine.currentNode()
    }

    if (output is EngineOutput.DrawCardRequest) {

      val card = when {
        output.random == true -> cardManager.drawCard()
        output.value != null -> cardManager.getByValue(output.value!!)
        output.image != null -> cardManager.drawCard()//cardManager.getByImage(output.image)
        else -> null
      }

      requireNotNull(card) { "Card not found: $output" }

      val instance = metaManager.addCard(card)
      return EngineOutput.ShowCard(
        image = instance.image,
        id = instance.id
      ) to null
    }

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

  fun getPlayerCards(): List<UiCard> {
    return metaManager.getCards().map {
      UiCard(
        id = it.id,
        image = it.image
      )
    }
  }
  fun rollDice(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    val node = engine.currentNode() as? SceneNode.DiceRoll ?: return step()

    engine.state.diceResult = engine.dice.roll(node.sides)

//    val randomCard = cardManager.drawCard()
//    metaManager.addCard(randomCard)

    return step()
  }

  fun applyDiceModifier(extra: Float, usedCards: List<String>): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    val base = engine.state.diceResult ?: return next()

    val currentNode = engine.currentNode() as? SceneNode.DiceRoll
      ?: return next()

    val currentMod = engine.variables.getModifier(currentNode.modifierVar)

    engine.state.diceModifiedResult = base + currentMod + extra

    usedCards.forEach { cardId ->
      metaManager.consumeCard(cardId)
    }

    return next()
  }
  fun consumeCard(cardId: String) {
    metaManager.consumeCard(cardId)
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
    val output = engine.step()

    if (output is EngineOutput.ShowDice) {
      val cards = getPlayerCards()
      return output.copy(cards = cards) to engine.currentNode()
    }

    return output to engine.currentNode()
  }
  fun listSaves(): List<String> =
    saveManager.listSaves()
}