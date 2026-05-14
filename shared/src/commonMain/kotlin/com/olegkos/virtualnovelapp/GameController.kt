package com.olegkos.virtualnovelapp

import com.olegkos.save.SaveManager
import com.olegkos.save.metaStorage.MetaManager
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.ScenarioParser
import com.olegkos.vnengine.engine.BattlePhase
import com.olegkos.vnengine.engine.DiceDuelPhase
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.NodePointer
import com.olegkos.vnengine.engine.UiCard
import com.olegkos.vnengine.engine.VisibleCharacter
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.engine.cards.CardConfig
import com.olegkos.vnengine.engine.cards.CardData
import com.olegkos.vnengine.engine.cards.CardManager
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.game.GameLoader
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.SubClass.ClassStartingCard
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

  companion object {
    /** Синхронный println на каждый next сильно просаживает FPS (особенно анимации кубика). */
    private const val TRACE_ENGINE_STEPS = false
  }

  private val basePath = "game/"
  private val gameConfigPath = basePath + "game.json"

  private var engine: VnEngine? = null
  val requireEngine: VnEngine
    get() = engine ?: error("Engine not initialized")
  private var currentScenario: String = ""

  /** Превью init и выдача в Meta используют одну и ту же карту на слот (особенно для random). */
  private val startingCardPickCache = mutableMapOf<String, CardData>()

  lateinit var assets: AssetPathResolver
    private set

  lateinit var reader: AssetReader
    private set

  suspend fun init(): Pair<EngineOutput, SceneNode?> {
    println("ENGINE INSTANCE: ${System.identityHashCode(engine)}")

    startingCardPickCache.clear()

    val game = withContext(ioDispatcher) {
      loader.load(gameConfigPath)
    }

    this.assets = game.assets
    this.reader = assetReader
    currentScenario = game.scenarioPath

    val json = Json { ignoreUnknownKeys = true }

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

    println("LOAD CARDS FROM: ${game.cards}")

    val cardsList = if (game.cards.isBlank()) {
      emptyList()
    } else {
      val cardsRaw = assetReader.readText(basePath + game.cards)
      try {
        val wrapper = json.decodeFromString<CardConfig>(cardsRaw)
        wrapper.cards
      } catch (e: Exception) {
        println("❌ ERROR PARSING CARDS: ${e.message}")
        emptyList()
      }
    }
    println("CARDS LOADED: ${cardsList.size}")

    cardManager.setCards(cardsList)

    engine = VnEngine(state, dice).apply {
      addScenes(game.scenario.scenes)
    }

    return step()
  }

  fun next(option: Option? = null): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    if (TRACE_ENGINE_STEPS) {
      println("👉 NEXT CALLED option=$option pointer=${engine.state.pointer}")
    }

    engine.advanceExternal(option)

    val output = engine.step()

    if (TRACE_ENGINE_STEPS) {
      println("👉 ENGINE OUTPUT = $output")
      println("👉 NODE = ${engine.currentNode()}")
    }

    return when (output) {

      is EngineOutput.ShowDice -> {
        val cards = getPlayerCards()
        output.copy(cards = cards) to engine.currentNode()
      }

      is EngineOutput.ShowDiceDuel -> {
        val cards = getPlayerCards()
        output.copy(cards = cards) to engine.currentNode()
      }

      is EngineOutput.DrawCardRequest -> {
        val card = resolveCardPick(
          random = output.random,
          value = output.value,
          image = output.image
        )

        requireNotNull(card) { "Card not found: $output" }

        val instance = metaManager.addCard(card)

        EngineOutput.ShowCard(
          image = instance.image,
          id = instance.id
        ) to null
      }

      is EngineOutput.JumpScenarioOutput -> output to null
      is EngineOutput.EndOfScene -> output to null

      else -> output to engine.currentNode()
    }
  }

  suspend fun switchScenario(path: String): Pair<EngineOutput, SceneNode?> {
    val scenario = loadScenario(path)

    engine?.addScenes(scenario.scenes)
    engine?.state?.pointer = NodePointer(scenario.startSceneId, 0)

    currentScenario = path

    return step()
  }

  fun getPlayerCards(): List<UiCard> {
    if (engine == null) return emptyList()
    return metaManager.getCards().map {
      UiCard(
        id = it.id,
        image = it.image,
        value = it.value
      )
    }
  }

  fun visibleCharactersInWorld(): List<VisibleCharacter> =
    engine?.state?.visibleCharacters ?: emptyList()

  /** Превью стартовой карты для экрана init; результат закэширован на (classId, slot). */
  fun previewStartingCard(
    classId: String,
    slotIndex: Int,
    spec: ClassStartingCard
  ): Pair<Int, String>? {
    val key = startingSlotKey(classId, slotIndex, spec)
    startingCardPickCache[key]?.let { return it.value to it.image }
    val card = resolveCardPick(random = spec.random, value = spec.value, image = spec.image)
      ?: return null
    startingCardPickCache[key] = card
    return card.value to card.image
  }

  /** Выдать стартовые карты выбранного класса — те же экземпляры, что в превью init (если слот уже открывали). */
  fun grantStartingCards(classId: String, specs: List<ClassStartingCard>) {
    specs.forEachIndexed { index, spec ->
      val key = startingSlotKey(classId, index, spec)
      val card = startingCardPickCache.remove(key)
        ?: resolveCardPick(random = spec.random, value = spec.value, image = spec.image)
      if (card == null) {
        println("⚠️ STARTING CARD SKIPPED (пустая колода или неверный spec): $spec")
        return@forEachIndexed
      }
      metaManager.addCard(card)
    }
    val prefix = "$classId|"
    startingCardPickCache.keys.filter { it.startsWith(prefix) }.forEach { startingCardPickCache.remove(it) }
  }

  private fun startingSlotKey(classId: String, slotIndex: Int, spec: ClassStartingCard): String =
    "$classId|$slotIndex|${specFingerprint(spec)}"

  private fun specFingerprint(spec: ClassStartingCard): String =
    listOf(
      spec.random?.toString() ?: "n",
      spec.value?.toString() ?: "n",
      spec.image ?: "n"
    ).joinToString("|")

  private fun resolveCardPick(
    random: Boolean?,
    value: Int?,
    image: String?
  ): CardData? = try {
    when {
      random == true -> cardManager.drawCard()
      value != null -> cardManager.getByValue(value)
      image != null -> cardManager.getByImage(image)
      else -> null
    }
  } catch (_: IllegalArgumentException) {
    null
  }

  fun rollDice(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    val node = engine.currentNode() as? SceneNode.DiceRoll ?: return step()

    val diceId = buildDiceId()

    val saved = metaManager.getDiceResult(diceId)

    val result = if (saved != null) {
      saved
    } else {
      val roll = engine.dice.roll(node.sides)
      metaManager.saveDiceResult(diceId, roll)
      roll
    }

    engine.state.diceResult = result
    engine.state.diceModifiedResult = null
    println("DICE ID: $diceId RESULT: $result (saved=${saved != null})")
    return step()
  }

  fun applyDiceModifier(extra: Float, usedCards: List<String>): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    engine.state.diceResult ?: return next()

    val currentNode = engine.currentNode() as? SceneNode.DiceRoll
      ?: return next()

    val currentMod = engine.variables.getModifier(currentNode.modifierVar)

    engine.state.diceModifiedResult = engine.state.diceResult!! + currentMod + extra

    usedCards.forEach { cardId ->
      consumeCard(cardId)
    }

    return next()
  }

  fun consumeCard(cardId: String) {
    metaManager.consumeCard(cardId)
  }

  fun saveGame(slot: String) {
    val engine = engine ?: return

    println("🎮 SAVE GAME CALLED")
    println("CURRENT SCENARIO: $currentScenario")
    println("POINTER: ${engine.state.pointer}")

    saveManager.save(
      slot = slot,
      state = engine.state,
      scenario = currentScenario
    )
  }

  fun battleChooseFight(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.battleChooseFight()
    return step()
  }

  fun battleChooseEscape(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.battleChooseEscape()
    return step()
  }

  fun battleRoll(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    val node = engine.currentNode() as? SceneNode.Battle ?: return step()
    val bs = engine.state.battle ?: return step()

    val sides = when (bs.phase) {
      BattlePhase.HORROR -> node.phases.horror?.sides
      BattlePhase.COMBAT -> node.phases.combat.sides
      BattlePhase.ESCAPE -> node.escape?.sides
      else -> null
    } ?: return step()

    val result = engine.dice.roll(sides)

    engine.state.diceResult = result
    engine.state.diceModifiedResult = null

    println("BATTLE ROLL phase=${bs.phase} sides=$sides result=$result")

    return step()
  }

  fun battleApplyModifier(extra: Float, usedCards: List<String>): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    val node = engine.currentNode() as? SceneNode.Battle ?: return step()
    val bs = engine.state.battle ?: return step()
    val base = engine.state.diceResult ?: return step()

    val modifierVar = when (bs.phase) {
      BattlePhase.HORROR -> node.phases.horror?.modifierVar
      BattlePhase.COMBAT -> node.phases.combat.modifierVar
      BattlePhase.ESCAPE -> node.escape?.modifierVar
      else -> null
    } ?: return step()

    val currentMod = engine.variables.getModifier(modifierVar)
    engine.state.diceModifiedResult = base + currentMod + extra

    usedCards.forEach { cardId ->
      consumeCard(cardId)
    }

    return step()
  }

  fun battleContinue(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.currentNode() as? SceneNode.Battle ?: return step()
    return step()
  }

  fun battlePostCombatVnNext(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.currentNode() as? SceneNode.Battle ?: return step()
    engine.battlePostCombatVnNext()
    return step()
  }

  suspend fun loadSave(slot: String): Pair<EngineOutput, SceneNode?> {

    val loaded = saveManager.load(slot)
      ?: return EngineOutput.Loading to null

    startingCardPickCache.clear()

    val scenario = loadScenario(loaded.scenario)

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

    return when (output) {

      is EngineOutput.ShowDiceDuel -> {
        val cards = getPlayerCards()
        output.copy(cards = cards) to engine.currentNode()
      }

      is EngineOutput.ShowDice -> {
        val cards = getPlayerCards()
        output.copy(cards = cards) to engine.currentNode()
      }

      else -> output to engine.currentNode()
    }
  }

  private fun buildDiceId(): String {
    val engine = requireEngine
    val pointer = engine.state.pointer

    return "$currentScenario|${pointer.sceneId}|${pointer.nodeIndex}"
  }

  fun diceDuelRoll(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.currentNode() as? SceneNode.DiceDuel ?: return step()
    val ds = engine.state.diceDuel ?: return step()

    if (ds.phase == DiceDuelPhase.PLAYER_ROLL) {
      engine.diceDuelRollBoth()
    }

    return step()
  }

  fun diceDuelApplyModifier(extra: Float, usedCards: List<String>): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null

    engine.diceDuelApplyModifier(extra)

    usedCards.forEach { cardId ->
      consumeCard(cardId)
    }

    return step()
  }

  fun diceDuelContinue(): Pair<EngineOutput, SceneNode?> {
    val engine = engine ?: return EngineOutput.Loading to null
    engine.diceDuelContinue()
    return step()
  }

  fun listSaves(): List<String> =
    saveManager.listSaves()
}