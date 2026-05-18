package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.EngineOutput.JumpScenarioOutput
import com.olegkos.vnengine.engine.EngineOutput.ShowAcademyHub
import com.olegkos.vnengine.engine.EngineOutput.EndOfScene
import com.olegkos.vnengine.engine.academy.AcademyBuildingConfig
import com.olegkos.vnengine.engine.academy.AcademyConfig
import com.olegkos.vnengine.engine.academy.AcademyHubPhase
import com.olegkos.vnengine.engine.academy.AcademyRandomEventConfig
import com.olegkos.vnengine.engine.academy.AcademyRequirementJson
import com.olegkos.vnengine.engine.academy.AcademyPlaybackStep
import com.olegkos.vnengine.engine.academy.AcademyState
import com.olegkos.vnengine.engine.academy.toRequirement
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.scene.SceneNode
import kotlin.random.Random

internal fun VnEngine.handleAcademyHubNode(node: SceneNode.AcademyHub): EngineOutput {
  val gs = state.academy ?: AcademyState(configPath = node.configFile).also { state.academy = it }
  if (gs.configPath != node.configFile) {
    state.academy = AcademyState(configPath = node.configFile).also { state.academy = it }
  }
  return buildAcademyHubOutput(node, requireNotNull(state.academy))
}

fun VnEngine.loadAcademyConfig(config: AcademyConfig) {
  state.academyConfig = config
}

fun VnEngine.academySelectBuilding(buildingId: String?) {
  val gs = state.academy ?: return
  if (gs.hubPhase != AcademyHubPhase.PLANNING || gs.buildUsedToday) return
  gs.selectedBuildingId = buildingId
}

fun VnEngine.academySetActivity(phaseId: String, activityId: String?) {
  val gs = state.academy ?: return
  if (gs.hubPhase != AcademyHubPhase.PLANNING) return
  if (activityId == null) {
    gs.planByPhase.remove(phaseId)
  } else {
    gs.planByPhase[phaseId] = activityId
  }
}

fun VnEngine.academyCommitDay(returnScenario: String): EngineOutput? {
  val node = currentAcademyHubNode() ?: return null
  val gs = state.academy ?: return null
  val config = state.academyConfig ?: return null
  if (gs.hubPhase != AcademyHubPhase.PLANNING) return null
  if (validateAcademyPlan(config, gs) != null) return null

  gs.returnScenario = returnScenario
  gs.randomEventId = pickAcademyRandomEvent(config)?.id
  gs.playbackQueue = buildAcademyPlaybackQueue(config, gs)
  gs.playbackIndex = 0
  gs.hubPhase = AcademyHubPhase.PLAYBACK
  gs.returnPointer = state.pointer.copy()
  gs.buildUsedToday = gs.selectedBuildingId != null

  return academyStartCurrentPlayback()
}

/** После EndOfScene микро-сценария: следующий jump или null = вернуться в хаб. */
fun VnEngine.academyAdvanceAfterScenario(): EngineOutput? {
  val gs = state.academy ?: return null
  if (gs.hubPhase != AcademyHubPhase.PLAYBACK) return null

  gs.playbackQueue.getOrNull(gs.playbackIndex)?.let { completed ->
    applyBuildingUpgrade(completed)
  }

  gs.playbackIndex++
  if (gs.playbackIndex < gs.playbackQueue.size) {
    return beginAcademyPlaybackStep(gs.playbackQueue[gs.playbackIndex])
  }

  academyFinishDay()
  gs.hubPhase = AcademyHubPhase.PLANNING
  gs.selectedBuildingId = null
  gs.planByPhase.clear()
  gs.playbackQueue = emptyList()
  gs.playbackIndex = 0
  gs.randomEventId = null
  gs.buildUsedToday = false
  return null
}

fun VnEngine.academyHubReturnPointer(): NodePointer? = state.academy?.returnPointer
fun VnEngine.academyHubReturnScenario(): String? = state.academy?.returnScenario

private fun VnEngine.academyStartCurrentPlayback(): EngineOutput {
  val gs = state.academy ?: return EndOfScene
  val step = gs.playbackQueue.getOrNull(gs.playbackIndex) ?: return EndOfScene
  state.scenarioStack.addLast(state.pointer.copy())
  advance()
  return beginAcademyPlaybackStep(step)
}

private fun VnEngine.beginAcademyPlaybackStep(step: AcademyPlaybackStep): EngineOutput {
  if (step.phaseId.isNotEmpty()) {
    state.variables["academy_phase_id"] = GameValue.StringVal(step.phaseId)
    state.variables["academy_phase_label"] = GameValue.StringVal(step.phaseLabel)
  } else {
    state.variables.remove("academy_phase_id")
    state.variables.remove("academy_phase_label")
  }
  if (step.activityLabel.isNotEmpty()) {
    state.variables["academy_activity_label"] = GameValue.StringVal(step.activityLabel)
  } else {
    state.variables.remove("academy_activity_label")
  }
  return JumpScenarioOutput(step.scenarioFile)
}

fun VnEngine.resetScenarioEntry(startSceneId: String) {
  state.pointer = NodePointer(startSceneId, 0)
  state.diceResult = null
  state.diceModifiedResult = null
  state.pendingDiceJumpScene = null
  state.pendingOutput = null
  state.waitingForUi = false
}

private fun VnEngine.academyFinishDay() {
  val config = state.academyConfig ?: return
  val dayVar = config.dayVar
  val current = when (val v = state.variables[dayVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }
  state.variables[dayVar] = GameValue.IntVal(current + 1)
}

private fun VnEngine.buildAcademyPlaybackQueue(
  config: AcademyConfig,
  gs: AcademyState,
): List<AcademyPlaybackStep> {
  val queue = mutableListOf<AcademyPlaybackStep>()

  gs.selectedBuildingId?.let { buildId ->
    val building = config.buildings.firstOrNull { it.id == buildId }
    val nextTier = building?.let { b ->
      val current = buildingLevel(b)
      b.levels.filter { it.level > current }.minByOrNull { it.level }
    }
    buildingScenarioFor(config, buildId)?.let { path ->
      queue.add(
        AcademyPlaybackStep(
          scenarioFile = path,
          activityLabel = building?.label ?: "Стройка",
          buildingId = buildId,
          upgradeToLevel = nextTier?.level,
        )
      )
    }
  }

  val random = gs.randomEventId?.let { id -> config.randomEvents.firstOrNull { it.id == id } }

  for (phase in config.phases) {
    val activityId = gs.planByPhase[phase.id] ?: continue
    val activity = resolveAcademyActivity(config, activityId) ?: continue
    queue.add(
      AcademyPlaybackStep(
        scenarioFile = activity.scenarioFile,
        phaseId = phase.id,
        phaseLabel = phase.label,
        activityLabel = activity.label,
      )
    )
    if (random != null && random.afterPhase == phase.id) {
      queue.add(
        AcademyPlaybackStep(
          scenarioFile = random.scenarioFile,
          phaseLabel = "Случайное событие",
          activityLabel = random.id,
        )
      )
    }
  }

  return queue
}

private fun VnEngine.pickAcademyRandomEvent(config: AcademyConfig): AcademyRandomEventConfig? {
  val eligible = config.randomEvents
    .filter { event -> meetsRequires(event.requires) }
    .filter { it.weight > 0 }
  if (eligible.isEmpty()) return null

  val total = eligible.sumOf { it.weight.coerceAtLeast(1) }
  var roll = Random.nextInt(total) + 1
  var picked = eligible.last()
  for (event in eligible) {
    roll -= event.weight
    if (roll <= 0) {
      picked = event
      break
    }
  }
  return if (Random.nextDouble() < picked.chance.coerceIn(0.0, 1.0)) picked else null
}

private fun VnEngine.buildingScenarioFor(
  config: AcademyConfig,
  buildingId: String,
): String? {
  val building = config.buildings.firstOrNull { it.id == buildingId } ?: return null
  val currentLevel = buildingLevel(building)
  val next = building.levels.filter { it.level > currentLevel }.minByOrNull { it.level } ?: return null
  if (!meetsRequires(next.requires)) return null
  return next.scenarioFile
}

private fun VnEngine.buildingLevel(building: AcademyBuildingConfig): Int =
  when (val v = state.variables[building.levelVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }

private fun VnEngine.applyBuildingUpgrade(step: AcademyPlaybackStep) {
  val buildingId = step.buildingId ?: return
  val targetLevel = step.upgradeToLevel ?: return
  val building = state.academyConfig?.buildings?.firstOrNull { it.id == buildingId } ?: return
  state.variables[building.levelVar] = GameValue.IntVal(targetLevel)
}

internal data class ResolvedAcademyActivity(
  val id: String,
  val label: String,
  val scenarioFile: String,
  val phases: List<String>,
  val requires: List<AcademyRequirementJson>,
  val fromBuildingId: String? = null,
)

private fun VnEngine.collectAcademyActivities(config: AcademyConfig): List<ResolvedAcademyActivity> {
  val list = mutableListOf<ResolvedAcademyActivity>()
  for (act in config.activities) {
    list.add(
      ResolvedAcademyActivity(
        id = act.id,
        label = act.label,
        scenarioFile = act.scenarioFile,
        phases = act.phases,
        requires = act.requires,
      )
    )
  }
  for (building in config.buildings) {
    val builtLevel = buildingLevel(building)
    for (levelCfg in building.levels.filter { it.level <= builtLevel }) {
      for (act in levelCfg.activities) {
        list.add(
          ResolvedAcademyActivity(
            id = "${building.id}:${act.id}",
            label = act.label,
            scenarioFile = act.scenarioFile,
            phases = act.phases,
            requires = act.requires,
            fromBuildingId = building.id,
          )
        )
      }
    }
  }
  return list
}

private fun VnEngine.resolveAcademyActivity(
  config: AcademyConfig,
  activityId: String,
): ResolvedAcademyActivity? =
  collectAcademyActivities(config).firstOrNull { it.id == activityId }

private fun VnEngine.validateAcademyPlan(config: AcademyConfig, gs: AcademyState): String? {
  for (phase in config.phases) {
    val activityId = gs.planByPhase[phase.id]
      ?: return "Выберите действие: ${phase.label}"
    val activity = resolveAcademyActivity(config, activityId)
      ?: return "Неизвестное действие"
    if (activity.phases.isNotEmpty() && phase.id !in activity.phases) {
      return "${activity.label} недоступно в фазе ${phase.label}"
    }
    if (!meetsRequires(activity.requires)) {
      return "${activity.label} пока недоступно"
    }
  }
  gs.selectedBuildingId?.let { id ->
    if (buildingScenarioFor(config, id) == null) {
      return "Постройка недоступна"
    }
  }
  return null
}

private fun VnEngine.meetsRequires(reqs: List<AcademyRequirementJson>): Boolean =
  reqs.all { matchesRequirement(it.toRequirement()) }

fun VnEngine.matchesRequirement(req: SceneNode.WeightedRandomJump.Requirement): Boolean {
  val current = state.variables[req.variable] ?: return false
  val expected = req.value

  fun cmpNumbers(a: Float, b: Float): Boolean = when (req.op) {
    SceneNode.WeightedRandomJump.Op.EQ -> a == b
    SceneNode.WeightedRandomJump.Op.NEQ -> a != b
    SceneNode.WeightedRandomJump.Op.GTE -> a >= b
    SceneNode.WeightedRandomJump.Op.LTE -> a <= b
    SceneNode.WeightedRandomJump.Op.GT -> a > b
    SceneNode.WeightedRandomJump.Op.LT -> a < b
  }

  return when {
    current is GameValue.IntVal && expected is GameValue.IntVal ->
      cmpNumbers(current.value.toFloat(), expected.value.toFloat())
    current is GameValue.FloatVal && expected is GameValue.FloatVal ->
      cmpNumbers(current.value, expected.value)
    current is GameValue.IntVal && expected is GameValue.FloatVal ->
      cmpNumbers(current.value.toFloat(), expected.value)
    current is GameValue.FloatVal && expected is GameValue.IntVal ->
      cmpNumbers(current.value, expected.value.toFloat())
    current is GameValue.Bool && expected is GameValue.Bool ->
      current.value == expected.value
    current is GameValue.StringVal && expected is GameValue.StringVal ->
      current.value == expected.value
    else -> false
  }
}

fun VnEngine.buildAcademyHubOutput(node: SceneNode.AcademyHub, gs: AcademyState): ShowAcademyHub {
  val config = state.academyConfig
    ?: error("Academy config not loaded. Call loadAcademyConfig first.")

  if (gs.hubPhase == AcademyHubPhase.PLANNING && gs.playbackQueue.isEmpty()) {
    gs.buildUsedToday = false
  }

  val day = when (val v = state.variables[config.dayVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }

  val validationError = if (gs.hubPhase == AcademyHubPhase.PLANNING) {
    validateAcademyPlan(config, gs)
  } else null

  val groups = config.buildings
    .groupBy { it.group }
    .map { (groupId, buildings) ->
      EngineOutput.AcademyBuildingGroupUi(
        id = groupId,
        label = groupLabel(groupId),
        buildings = buildings.map { b -> buildingToUi(b, gs) },
      )
    }

  val phases = config.phases.map { phase ->
    val selectedId = gs.planByPhase[phase.id]
    EngineOutput.AcademyTimeSlotUi(
      phaseId = phase.id,
      label = phase.label,
      selectedActivityId = selectedId,
      activities = collectAcademyActivities(config)
        .filter { act ->
          (act.phases.isEmpty() || phase.id in act.phases) &&
            meetsRequires(act.requires)
        }
        .map {
          EngineOutput.AcademyActivityOptionUi(
            id = it.id,
            label = it.label,
            fromBuilding = it.fromBuildingId != null,
          )
        },
    )
  }

  return ShowAcademyHub(
    background = config.background,
    day = day,
    planning = gs.hubPhase == AcademyHubPhase.PLANNING,
    buildingGroups = groups,
    timeSlots = phases,
    canCommit = validationError == null && gs.hubPhase == AcademyHubPhase.PLANNING,
    commitBlockedReason = validationError,
    selectedBuildingId = gs.selectedBuildingId,
    buildUsedToday = gs.buildUsedToday,
  )
}

private fun VnEngine.buildingToUi(
  building: AcademyBuildingConfig,
  gs: AcademyState,
): EngineOutput.AcademyBuildingUi {
  val level = buildingLevel(building)
  val next = building.levels.filter { it.level > level }.minByOrNull { it.level }
  val levelOk = next == null || meetsRequires(next.requires)
  val canBuildToday = gs.hubPhase == AcademyHubPhase.PLANNING &&
    !gs.buildUsedToday &&
    next != null &&
    levelOk
  val statusLabel = when {
    level == 0 && next == null -> "Нет улучшений"
    level == 0 -> "Можно построить"
    next != null && levelOk -> "Ур. $level · улучшить до ${next.level}"
    next != null && !levelOk -> "Ур. $level · условия не выполнены"
    else -> "Построено (ур. $level)"
  }
  val lockedReason = when {
    !canBuildToday && gs.buildUsedToday -> "Сегодня стройка уже запланирована"
    !canBuildToday && next == null && level > 0 -> null
    !canBuildToday && next == null -> "Недоступно"
    !canBuildToday && !levelOk -> "Условия не выполнены"
    else -> null
  }
  return EngineOutput.AcademyBuildingUi(
    id = building.id,
    label = building.label,
    group = building.group,
    level = level,
    xPercent = building.xPercent,
    yPercent = building.yPercent,
    enabled = canBuildToday,
    lockedReason = lockedReason,
    selected = gs.selectedBuildingId == building.id,
    statusLabel = statusLabel,
    isBuilt = level > 0,
  )
}

private fun groupLabel(groupId: String): String = when (groupId) {
  "study" -> "Учёба"
  "life" -> "Быт"
  else -> groupId.replaceFirstChar { it.uppercase() }
}

fun VnEngine.currentAcademyHubNode(): SceneNode.AcademyHub? =
  currentNode() as? SceneNode.AcademyHub
