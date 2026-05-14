  package com.olegkos.virtualnovelapp

  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.setValue
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.olegkos.save.SaveManager
  import com.olegkos.vnengine.GameLoading.AssetReader
  import com.olegkos.vnengine.engine.EngineOutput
  import com.olegkos.vnengine.engine.VisibleCharacter
  import com.olegkos.vnengine.engine.asserts.AssetPathResolver
  import com.olegkos.vnengine.engine.variables.GameValue
  import com.olegkos.vnengine.engine.variables.resolve
  import com.olegkos.vnengine.scene.Option
  import com.olegkos.vnengine.scene.SceneNode
  import com.olegkos.vnengine.scene.SubClass
  import kotlinx.coroutines.launch

  class GameViewModel(
    private val controller: GameController,
    private val saveManager: SaveManager
  ) : ViewModel() {

    val assets: AssetPathResolver
      get() = controller.assets

    val reader: AssetReader
      get() = controller.reader

    var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
      private set

    var currentNode by mutableStateOf<SceneNode?>(null)
      private set

    val visibleCharacters: List<VisibleCharacter>
      get() = controller.visibleCharactersInWorld()

    init {
      viewModelScope.launch {
        val (output, node) = controller.init()
        currentOutput = output
        currentNode = node
      }
    }

    fun next(option: Option? = null) {
      val result = controller.next(option)
      val output = result.first

      when (output) {

        is EngineOutput.JumpScenarioOutput -> {
          viewModelScope.launch {
            val (newOutput, node) =
              controller.switchScenario(output.scenarioFile)

            currentOutput = newOutput
            currentNode = node
          }
        }

        else -> {
          currentOutput = output
          currentNode = result.second
        }
      }
    }
    fun rollDice() {
      val (output, node) = controller.rollDice()
      currentOutput = output
      currentNode = node
    }

    fun saveGame(slot: String) {
      controller.saveGame(slot)
    }

    fun loadSave(slot: String) {
      viewModelScope.launch {
        val (output, node) = controller.loadSave(slot)
        currentOutput = output
        currentNode = node
      }
    }

    fun initGame(
      playerName: String,
      selectedClass: SubClass.GameClass?,
      playerNameVar: String,
      classVar: String?
    ) {
      val engine = controller.requireEngine

      engine.variables.set(playerNameVar, GameValue.StringVal(playerName))

      if (selectedClass != null && classVar != null) {
        engine.variables.set(classVar, GameValue.StringVal(selectedClass.id))

        selectedClass.stats.forEach { (key, value) ->
          engine.variables.set(key, value.resolve())
        }
      }

      selectedClass?.let { cls ->
        controller.grantStartingCards(cls.id, cls.startingCards)
      }

      engine.state.isGameInitialized = true

      next()
    }

    fun previewStartingCard(
      classId: String,
      slotIndex: Int,
      spec: SubClass.ClassStartingCard
    ): Pair<Int, String>? =
      controller.previewStartingCard(classId, slotIndex, spec)

    fun applyDiceModifier(extra: Float, usedCards: List<String>) {
      val (output, node) = controller.applyDiceModifier(extra, usedCards)

      currentOutput = output
      currentNode = node ?: controller.requireEngine.currentNode()
    }
    fun jumpScenario(path: String) {
      viewModelScope.launch {
        val (output, node) = controller.switchScenario(path)
        currentOutput = output
        currentNode = node
      }
    }
    fun getCards() = controller.getPlayerCards()
    fun battleChooseFight() {
      val (output, node) = controller.battleChooseFight()
      currentOutput = output
      currentNode = node
    }

    fun battleChooseEscape() {
      val (output, node) = controller.battleChooseEscape()
      currentOutput = output
      currentNode = node
    }

    fun battleRoll() {
      val (output, node) = controller.battleRoll()
      currentOutput = output
      currentNode = node
    }

    fun battleApplyModifier(extra: Float, usedCards: List<String>) {
      val (output, node) = controller.battleApplyModifier(extra, usedCards)
      currentOutput = output
      currentNode = node ?: controller.requireEngine.currentNode()
    }

    fun battleContinue() {
      val (output, node) = controller.battleContinue()
      currentOutput = output
      currentNode = node
    }

    fun battlePostCombatVnNext() {
      val (output, node) = controller.battlePostCombatVnNext()
      currentOutput = output
      currentNode = node
    }

    fun diceDuelRoll() {
      val (output, node) = controller.diceDuelRoll()
      currentOutput = output
      currentNode = node
    }

    fun diceDuelApplyModifier(extra: Float, usedCards: List<String>) {
      val (output, node) = controller.diceDuelApplyModifier(extra, usedCards)
      currentOutput = output
      currentNode = node ?: controller.requireEngine.currentNode()
    }

    fun diceDuelContinue() {
      val (output, node) = controller.diceDuelContinue()
      currentOutput = output
      currentNode = node
    }

    fun useCards(cardIds: List<String>) {
      cardIds.forEach { controller.consumeCard(it) }
    }

    fun consumeCard(cardId: String) {
      controller.consumeCard(cardId)
    }
    fun listSaves(): List<String> =
      saveManager.listSaves()
  }