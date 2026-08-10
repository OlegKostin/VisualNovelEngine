  package com.olegkos.virtualnovelapp

  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.setValue
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.olegkos.save.SaveManager
  import com.olegkos.vnengine.GameLoading.AssetReader
  import com.olegkos.vnengine.engine.academy.AcademyPlanMode
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

    val isReady: Boolean
      get() = controller.isReady

    var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
      private set

    var currentNode by mutableStateOf<SceneNode?>(null)
      private set

    /** Увеличивается при switchScenario — сброс overlay image/card в UI. */
    var sceneLayerResetToken by mutableStateOf(0)
      private set

    val visibleCharacters: List<VisibleCharacter>
      get() = controller.visibleCharactersInWorld()

    fun setSceneBackground(path: String) {
      controller.setSceneBackground(path)
    }

    init {
      viewModelScope.launch {
        val (output, node) = controller.init()
        val (resolved, resolvedNode) = resolveJumpChain(output, node)
        currentOutput = resolved
        currentNode = resolvedNode
      }
    }

    fun next(option: Option? = null) {
      val result = controller.next(option)
      applyOutput(result.first, result.second)
    }
    fun rollDice() {
      val (output, node) = controller.rollDice()
      currentOutput = output
      currentNode = node
    }

    fun saveGame(slot: String, previewPng: ByteArray? = null) {
      controller.saveGame(slot, previewPng)
    }

    fun deleteSave(slot: String) {
      controller.deleteSave(slot)
    }

    fun savePreviewPng(slot: String): ByteArray? =
      controller.savePreviewPng(slot)

    fun saveTimestampMillis(slot: String): Long? =
      controller.saveTimestampMillis(slot)

    fun loadSave(slot: String) {
      viewModelScope.launch {
        val (output, node) = controller.loadSave(slot)
        val (resolved, resolvedNode) = resolveJumpChain(output, node)
        currentOutput = resolved
        currentNode = resolvedNode
      }
    }

    fun initGame(
      playerName: String,
      selectedClass: SubClass.GameClass?,
      playerNameVar: String,
      classVar: String?
    ) {
      val engine = controller.requireEngine

      val nameVal = GameValue.StringVal(playerName.trim())
      listOf("my_name", "player_name", playerNameVar).distinct().forEach { varName ->
        engine.variables.set(varName, nameVal)
      }

      if (selectedClass != null && classVar != null) {
        engine.variables.set(classVar, GameValue.StringVal(selectedClass.id))

        engine.state.statCapsInt = statCapsFromClassStats(selectedClass.stats)

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

    private fun statCapsFromClassStats(stats: Map<String, GameValue>): Map<String, Int> {
      val caps = mutableMapOf<String, Int>()
      for (key in listOf("health", "mental_health")) {
        stats[key]?.let { gv ->
          when (val r = gv.resolve()) {
            is GameValue.IntVal -> caps[key] = r.value
            is GameValue.FloatVal -> caps[key] = r.value.toInt()
            else -> Unit
          }
        }
      }
      return caps
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
        val (output, node) = switchScenario(path)
        val (resolved, resolvedNode) = resolveJumpChain(output, node)
        currentOutput = resolved
        currentNode = resolvedNode
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

    fun cardGameConfirmDraft(metaSelectedIds: List<String>, poolSelectedIds: List<String>) {
      val (output, node) = controller.cardGameConfirmDraft(metaSelectedIds, poolSelectedIds)
      currentOutput = output
      currentNode = node
    }

    fun cardGameConfirmClash(selectedIds: List<String>) {
      val (output, node) = controller.cardGameConfirmClash(selectedIds)
      currentOutput = output
      currentNode = node
    }

    fun cardGameBattleContinue() {
      val (output, node) = controller.cardGameBattleContinue()
      currentOutput = output
      currentNode = node
    }

    fun cardGameBreakdownNext() {
      val (output, node) = controller.cardGameBreakdownNext()
      currentOutput = output
      currentNode = node
    }

    fun cardGameVnNext() {
      val (output, node) = controller.cardGameVnNext()
      currentOutput = output
      currentNode = node
    }

    fun cardGameFinish() {
      val (output, node) = controller.cardGameFinish()
      currentOutput = output
      currentNode = node
    }

    fun targetTapStart() {
      val (output, node) = controller.targetTapStart()
      currentOutput = output
      currentNode = node
    }

    fun targetTapHit(targetId: String) {
      val (output, node) = controller.targetTapHit(targetId)
      currentOutput = output
      currentNode = node
    }

    fun targetTapMiss(targetId: String) {
      val (output, node) = controller.targetTapMiss(targetId)
      currentOutput = output
      currentNode = node
    }

    fun targetTapContinueSpawn() {
      val (output, node) = controller.targetTapContinueSpawn()
      currentOutput = output
      currentNode = node
    }

    fun academyEnactLaw(lawId: String) {
      viewModelScope.launch {
        val (output, node) = controller.academyEnactLaw(lawId)
        applyOutput(output, node)
      }
    }

    fun academyQueueUnlock(unlockId: String?) {
      viewModelScope.launch {
        val (output, node) = controller.academyQueueUnlock(unlockId)
        applyOutput(output, node)
      }
    }

    fun academySelectBuilding(buildingId: String?) {
      viewModelScope.launch {
        val (output, node) = controller.academySelectBuilding(buildingId)
        applyOutput(output, node)
      }
    }

    fun academySetPlanMode(mode: AcademyPlanMode) {
      viewModelScope.launch {
        val (output, node) = controller.academySetPlanMode(mode)
        applyOutput(output, node)
      }
    }

    fun academySetFullDayActivity(activityId: String?) {
      viewModelScope.launch {
        val (output, node) = controller.academySetFullDayActivity(activityId)
        applyOutput(output, node)
      }
    }

    fun academySetActivity(phaseId: String, activityId: String?) {
      viewModelScope.launch {
        val (output, node) = controller.academySetActivity(phaseId, activityId)
        applyOutput(output, node)
      }
    }

    fun academyCommitDay() {
      viewModelScope.launch {
        val (output, node) = controller.academyCommitDay()
        applyOutput(output, node)
      }
    }

    fun academyDaySummaryContinue() {
      viewModelScope.launch {
        val (output, node) = controller.academyDaySummaryContinue()
        applyOutput(output, node)
      }
    }

    private fun applyOutput(output: EngineOutput, node: SceneNode?) {
      if (output is EngineOutput.JumpScenarioOutput) {
        viewModelScope.launch {
          val (resolved, resolvedNode) = resolveJumpChain(output, node)
          currentOutput = resolved
          currentNode = resolvedNode
        }
      } else {
        currentOutput = output
        currentNode = node
      }
    }

    /**
     * Если сценарий начинается с weightedRandomJump/jumpScenario (или цепочкой jump),
     * первый output может снова быть JumpScenarioOutput. Без разворота UI залипает на «Загрузка...».
     */
    private suspend fun resolveJumpChain(
      output: EngineOutput,
      node: SceneNode?
    ): Pair<EngineOutput, SceneNode?> {
      var out = output
      var n = node
      while (out is EngineOutput.JumpScenarioOutput) {
        val pair = switchScenario(out.scenarioFile)
        out = pair.first
        n = pair.second
      }
      return out to n
    }

    private suspend fun switchScenario(path: String): Pair<EngineOutput, SceneNode?> {
      sceneLayerResetToken++
      return controller.switchScenario(path)
    }

    fun useCards(cardIds: List<String>) {
      cardIds.forEach { controller.consumeCard(it) }
    }

    fun consumeCard(cardId: String) {
      controller.consumeCard(cardId)
    }
    fun listSaves(): List<String> =
      saveManager.listSaves()

    fun playerStatsUi(): PlayerStatsUi =
      controller.buildPlayerStatsUi()
  }