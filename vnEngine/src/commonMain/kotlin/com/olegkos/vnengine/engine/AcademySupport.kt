package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.EngineOutput.JumpScenarioOutput
import com.olegkos.vnengine.engine.EngineOutput.ShowAcademyDaySummary
import com.olegkos.vnengine.engine.EngineOutput.ShowAcademyHub
import com.olegkos.vnengine.engine.EngineOutput.EndOfScene
import com.olegkos.vnengine.engine.academy.AcademyBuildingConfig
import com.olegkos.vnengine.engine.academy.AcademyBuildingLevelConfig
import com.olegkos.vnengine.engine.academy.AcademyConfig
import com.olegkos.vnengine.engine.academy.AcademyDayKind
import com.olegkos.vnengine.engine.academy.AcademyHubPhase
import com.olegkos.vnengine.engine.academy.AcademyRandomEventConfig
import com.olegkos.vnengine.engine.academy.AcademyRequirementJson
import com.olegkos.vnengine.engine.academy.AcademyPlaybackStep
import com.olegkos.vnengine.engine.academy.AcademyLawOnEnactEffectJson
import com.olegkos.vnengine.engine.academy.AcademyScheduleScope
import com.olegkos.vnengine.engine.academy.AcademyState

import com.olegkos.vnengine.engine.academy.formatAcademyStatNumber
import com.olegkos.vnengine.engine.academy.formatLawDailyLine
import com.olegkos.vnengine.engine.academy.formatLawOnEnactLine
import com.olegkos.vnengine.engine.academy.isFloatStat
import com.olegkos.vnengine.engine.academy.randomDailyDelta
import com.olegkos.vnengine.engine.academy.readAcademyStatFloat
import com.olegkos.vnengine.engine.academy.resolvedOnEnact
import com.olegkos.vnengine.engine.academy.toRequirement
import kotlin.math.roundToInt
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

data class AcademyLawScenarioDone(
  val returnScenario: String,
  val returnPointer: NodePointer,
)

/** Принять закон (один раз): списать ресурсы, сценарий, эффекты, enactedVar = true. */
fun VnEngine.academyBeginLawEnact(lawId: String, returnScenario: String): EngineOutput? {
  val gs = state.academy ?: return null
  val config = state.academyConfig ?: return null
  if (gs.hubPhase != AcademyHubPhase.PLANNING) return null
  val law = config.laws.firstOrNull { it.id == lawId } ?: return null
  if (isLawEnacted(law)) return null
  if (!meetsRequires(law.requires)) return null
  if (!law.scheduleScope().availableOn(academyDayKind(config))) return null
  if (academyResources(config) < law.cost) return null

  spendAcademyResources(config, law.cost)

  val scenario = law.scenarioFile?.trim()?.takeIf { it.isNotEmpty() }
  if (scenario != null) {
    gs.pendingLawEnactId = lawId
    gs.lawReturnScenario = returnScenario
    gs.lawReturnPointer = state.pointer.copy()
    return JumpScenarioOutput(scenario)
  }

  completeLawEnact(law)
  val node = currentAcademyHubNode() ?: return null
  return buildAcademyHubOutput(node, gs)
}

/** После EndOfScene микро-сценария закона. */
fun VnEngine.academyFinishPendingLawScenario(): AcademyLawScenarioDone? {
  val gs = state.academy ?: return null
  val lawId = gs.pendingLawEnactId ?: return null
  val config = state.academyConfig ?: return null
  val law = config.laws.firstOrNull { it.id == lawId } ?: return null
  val retScenario = gs.lawReturnScenario ?: return null
  val retPointer = gs.lawReturnPointer ?: return null
  gs.pendingLawEnactId = null
  gs.lawReturnScenario = null
  gs.lawReturnPointer = null
  completeLawEnact(law)
  return AcademyLawScenarioDone(
    returnScenario = retScenario,
    returnPointer = retPointer,
  )
}

fun VnEngine.academyQueueUnlock(unlockId: String?) {
  val gs = state.academy ?: return
  val config = state.academyConfig ?: return
  if (gs.hubPhase != AcademyHubPhase.PLANNING) return
  if (unlockId == null) {
    gs.pendingUnlockId = null
    return
  }
  val unlock = config.unlockableActions.firstOrNull { it.id == unlockId } ?: return
  if (unlock.id in gs.activeUnlockIds) return
  if (!meetsRequires(unlock.unlockRequires)) return
  if (!unlock.queueScheduleScope().availableOn(academyDayKind(config))) return
  gs.pendingUnlockId = if (gs.pendingUnlockId == unlockId) null else unlockId
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
  gs.buildingsBuiltThisPlayback.clear()
  snapshotAcademyDayStart(config, gs)
  applyAcademyDailyLawEffects(config)
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
    if (!completed.isDaySummary) {
      applyBuildingUpgrade(completed)
    }
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
  gs.dayStartVars.clear()
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
  if (step.isDaySummary) {
    return buildAcademyDaySummaryOutput()
  }
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
  val gs = state.academy ?: return
  val dayVar = config.dayVar
  val current = when (val v = state.variables[dayVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }
  val newDay = current + 1
  state.variables[dayVar] = GameValue.IntVal(newDay)
  gs.buildingsBuiltThisPlayback.forEach { buildingId ->
    gs.buildingHighlightDay[buildingId] = newDay
  }
  gs.buildingsBuiltThisPlayback.clear()
  gs.pendingUnlockId?.let { id ->
    gs.activeUnlockIds.add(id)
    gs.pendingUnlockId = null
  }
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

  queue.add(
    AcademyPlaybackStep(
      scenarioFile = "",
      activityLabel = "Итоги дня",
      isDaySummary = true,
    )
  )

  return queue
}

private fun VnEngine.snapshotAcademyDayStart(config: AcademyConfig, gs: AcademyState) {
  gs.dayStartVars.clear()
  gs.dayStartVars[config.resourcesVar] = academyResources(config).toFloat()
  config.stats.forEach { stat ->
    gs.dayStartVars[stat.varName] = readAcademyStatFloat(state.variables, stat.varName)
  }
}

private fun VnEngine.applyAcademyDailyLawEffects(config: AcademyConfig) {
  for (law in config.laws) {
    if (!isLawEnacted(law)) continue
    for (daily in law.daily) {
      val delta = randomDailyDelta(daily.deltaMin, daily.deltaMax)
      applyAcademyStatDelta(config, daily.varName, delta)
    }
  }
}

fun VnEngine.buildAcademyDaySummaryOutput(): ShowAcademyDaySummary {
  val config = state.academyConfig
    ?: error("Academy config not loaded")
  val gs = state.academy
    ?: error("Academy state missing")

  val day = when (val v = state.variables[config.dayVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }

  val changes = buildList {
    add(
      buildAcademyDayVarChange(
        label = config.resourcesLabel,
        varName = config.resourcesVar,
        isFloat = false,
        dayStart = gs.dayStartVars,
      ),
    )
    config.stats.forEach { stat ->
      add(
        buildAcademyDayVarChange(
          label = stat.label,
          varName = stat.varName,
          isFloat = stat.isFloatStat(),
          dayStart = gs.dayStartVars,
        ),
      )
    }
  }

  return ShowAcademyDaySummary(day = day, changes = changes)
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
  if (academyResources(config) < next.cost) return null
  return next.scenarioFile
}

private fun VnEngine.buildingLevel(building: AcademyBuildingConfig): Int =
  when (val v = state.variables[building.levelVar]) {
    is GameValue.IntVal -> v.value
    is GameValue.FloatVal -> v.value.toInt()
    else -> 0
  }

/** Активности здания берутся только с максимального достигнутого уровня (не суммируются). */
private fun effectiveBuildingLevelConfig(
  building: AcademyBuildingConfig,
  builtLevel: Int,
): AcademyBuildingLevelConfig? {
  if (builtLevel <= 0) return null
  return building.levels.filter { it.level <= builtLevel }.maxByOrNull { it.level }
}

private fun VnEngine.academyDay(config: AcademyConfig): Int =
  academyIntVar(config.dayVar)

private fun VnEngine.academyIntVar(varName: String): Int =
  readAcademyStatFloat(state.variables, varName).roundToInt()

private fun VnEngine.buildAcademyDayVarChange(
  label: String,
  varName: String,
  isFloat: Boolean,
  dayStart: Map<String, Float>,
): EngineOutput.AcademyDayVarChangeUi {
  val before = dayStart[varName] ?: readAcademyStatFloat(state.variables, varName)
  val after = readAcademyStatFloat(state.variables, varName)
  val delta = after - before
  return EngineOutput.AcademyDayVarChangeUi(
    label = label,
    before = formatAcademyStatNumber(before, isFloat),
    after = formatAcademyStatNumber(after, isFloat),
    delta = formatDayDelta(delta, isFloat),
    deltaSign = when {
      delta > 0f -> 1
      delta < 0f -> -1
      else -> 0
    },
  )
}

private fun formatDayDelta(delta: Float, isFloat: Boolean): String {
  if (delta == 0f) return "0"
  val magnitude = formatAcademyStatNumber(kotlin.math.abs(delta), isFloat)
  return if (delta > 0) "+$magnitude" else "-$magnitude"
}

private fun VnEngine.applyAcademyStatDelta(
  config: AcademyConfig,
  varName: String,
  delta: Float,
) {
  if (delta == 0f) return
  if (varName == config.resourcesVar) {
    val intDelta = delta.roundToInt()
    if (intDelta < 0) spendAcademyResources(config, -intDelta)
    else if (intDelta > 0) {
      val cur = academyResources(config)
      state.variables[config.resourcesVar] = GameValue.IntVal(cur + intDelta)
    }
    return
  }
  val stat = config.stats.firstOrNull { it.varName == varName }
  if (stat?.isFloatStat() == true) {
    variables.modify(varName, GameValue.FloatVal(delta))
  } else {
    variables.modify(varName, GameValue.IntVal(delta.roundToInt()))
  }
}

private fun VnEngine.applyLawOnEnactEffect(
  config: AcademyConfig,
  effect: AcademyLawOnEnactEffectJson,
) {
  applyAcademyStatDelta(config, effect.varName, effect.delta.toFloat())
}

private fun VnEngine.academyResources(config: AcademyConfig): Int =
  academyIntVar(config.resourcesVar)

private fun VnEngine.spendAcademyResources(config: AcademyConfig, amount: Int) {
  if (amount <= 0) return
  val current = academyResources(config)
  state.variables[config.resourcesVar] = GameValue.IntVal((current - amount).coerceAtLeast(0))
}

private fun VnEngine.applyBuildingUpgrade(step: AcademyPlaybackStep) {
  val buildingId = step.buildingId ?: return
  val targetLevel = step.upgradeToLevel ?: return
  val config = state.academyConfig ?: return
  val gs = state.academy ?: return
  val building = config.buildings.firstOrNull { it.id == buildingId } ?: return
  val tier = building.levels.firstOrNull { it.level == targetLevel }
  state.variables[building.levelVar] = GameValue.IntVal(targetLevel)
  tier?.let { spendAcademyResources(config, it.cost) }
  gs.buildingsBuiltThisPlayback.add(buildingId)
}

internal data class ResolvedAcademyActivity(
  val id: String,
  val label: String,
  val scenarioFile: String,
  val phases: List<String>,
  val requires: List<AcademyRequirementJson>,
  val scheduleScope: AcademyScheduleScope = AcademyScheduleScope.ALWAYS,
  val fromBuildingId: String? = null,
  val fromUnlockableId: String? = null,
)

private fun VnEngine.collectAcademyActivities(
  config: AcademyConfig,
  gs: AcademyState,
): List<ResolvedAcademyActivity> {
  val list = mutableListOf<ResolvedAcademyActivity>()
  for (act in config.activities) {
    list.add(
      ResolvedAcademyActivity(
        id = act.id,
        label = act.label,
        scenarioFile = act.scenarioFile,
        phases = act.phases,
        requires = act.requires,
        scheduleScope = act.visitScopeFor(),
      )
    )
  }
  for (building in config.buildings) {
    val levelCfg = effectiveBuildingLevelConfig(building, buildingLevel(building)) ?: continue
    for (act in levelCfg.activities) {
      list.add(
        ResolvedAcademyActivity(
          id = "${building.id}:${act.id}",
          label = act.label,
          scenarioFile = act.scenarioFile,
          phases = act.phases,
          requires = act.requires,
          scheduleScope = act.visitScopeFor(building = building),
          fromBuildingId = building.id,
        )
      )
    }
  }
  for (unlock in config.unlockableActions) {
    if (unlock.id !in gs.activeUnlockIds) continue
    for (act in unlock.activities) {
      list.add(
        ResolvedAcademyActivity(
          id = "unlock:${unlock.id}:${act.id}",
          label = act.label,
          scenarioFile = act.scenarioFile,
          phases = act.phases,
          requires = act.requires,
          scheduleScope = act.visitScopeFor(unlock = unlock),
          fromUnlockableId = unlock.id,
        )
      )
    }
  }
  return list
}

private fun VnEngine.resolveAcademyActivity(
  config: AcademyConfig,
  activityId: String,
): ResolvedAcademyActivity? {
  val gs = state.academy ?: return null
  return collectAcademyActivities(config, gs).firstOrNull { it.id == activityId }
}

private fun VnEngine.validateAcademyPlan(config: AcademyConfig, gs: AcademyState): String? {
  val dayKind = academyDayKind(config)
  for (phase in config.phases) {
    val activityId = gs.planByPhase[phase.id]
      ?: return "Выберите действие: ${phase.label}"
    val activity = resolveAcademyActivity(config, activityId)
      ?: return "Неизвестное действие"
    if (activity.phases.isNotEmpty() && phase.id !in activity.phases) {
      return "${activity.label} недоступно в фазе ${phase.label}"
    }
    if (!activity.scheduleScope.availableOn(dayKind)) {
      return activity.scheduleScope.lockedReason(dayKind, "visit")
        ?: "${activity.label} недоступно сегодня"
    }
    if (!meetsRequires(activity.requires)) {
      return "${activity.label} пока недоступно"
    }
  }
  gs.selectedBuildingId?.let { id ->
    val building = config.buildings.firstOrNull { it.id == id }
      ?: return "Неизвестная постройка"
    if (!building.buildScheduleScope().availableOn(dayKind)) {
      return "${building.label}: строить сегодня нельзя (${dayKind.shortLabel.lowercase()})"
    }
    val next = building.levels.filter { it.level > buildingLevel(building) }.minByOrNull { it.level }
      ?: return "Постройка недоступна"
    if (!meetsRequires(next.requires)) return "Постройка недоступна"
    if (academyResources(config) < next.cost) return "Недостаточно ресурсов"
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

  val day = academyDay(config)
  val dayKind = academyDayKind(config)
  val week = config.weekSchedule
  val dayCycleLabel = when (dayKind) {
    AcademyDayKind.WEEKDAY -> "Будни ${week.dayIndexInBlock(day)}/${week.weekdays}"
    AcademyDayKind.WEEKEND -> "Выходные ${week.dayIndexInBlock(day)}/${week.weekendDays}"
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
        buildings = buildings.map { b -> buildingToUi(b, gs, dayKind) },
      )
    }

  val currentDay = academyDay(config)
  val phases = config.phases.map { phase ->
    val selectedId = gs.planByPhase[phase.id]
    EngineOutput.AcademyTimeSlotUi(
      phaseId = phase.id,
      label = phase.label,
      selectedActivityId = selectedId,
      activities = collectAcademyActivities(config, gs)
        .filter { act ->
          act.scheduleScope.availableOn(dayKind) &&
            (act.phases.isEmpty() || phase.id in act.phases) &&
            meetsRequires(act.requires)
        }
        .map {
          val buildingId = it.fromBuildingId
          EngineOutput.AcademyActivityOptionUi(
            id = it.id,
            label = it.label,
            fromBuilding = buildingId != null,
            fromUnlockable = it.fromUnlockableId != null,
            highlightBuilding = buildingId != null &&
              gs.buildingHighlightDay[buildingId] == currentDay,
          )
        },
    )
  }

  val unlockableUi = config.unlockableActions.map { unlock ->
    unlockableToUi(unlock, gs, dayKind)
  }
  val pendingUnlockLabel = gs.pendingUnlockId?.let { id ->
    config.unlockableActions.firstOrNull { it.id == id }?.label
  }

  val lawUi = config.laws.map { lawToUi(it, config, dayKind) }

  val statUi = config.stats.map { stat ->
    val value = readAcademyStatFloat(state.variables, stat.varName)
    EngineOutput.AcademyStatUi(
      varName = stat.varName,
      label = stat.label,
      displayValue = formatAcademyStatNumber(value, stat.isFloatStat()),
    )
  }

  return ShowAcademyHub(
    background = config.background,
    day = day,
    dayKindLabel = dayKind.label,
    dayCycleLabel = dayCycleLabel,
    planning = gs.hubPhase == AcademyHubPhase.PLANNING,
    resources = academyResources(config),
    resourcesLabel = config.resourcesLabel,
    stats = statUi,
    buildingGroups = groups,
    timeSlots = phases,
    canCommit = validationError == null && gs.hubPhase == AcademyHubPhase.PLANNING,
    commitBlockedReason = validationError,
    selectedBuildingId = gs.selectedBuildingId,
    buildUsedToday = gs.buildUsedToday,
    unlockableActions = unlockableUi,
    pendingUnlockLabel = pendingUnlockLabel,
    laws = lawUi,
  )
}

private fun VnEngine.isLawEnacted(law: com.olegkos.vnengine.engine.academy.AcademyLawConfig): Boolean =
  when (val v = state.variables[law.enactedVar]) {
    is GameValue.Bool -> v.value
    is GameValue.IntVal -> v.value != 0
    is GameValue.FloatVal -> v.value.toInt() != 0
    else -> false
  }

private fun VnEngine.completeLawEnact(law: com.olegkos.vnengine.engine.academy.AcademyLawConfig) {
  val config = state.academyConfig ?: return
  state.variables[law.enactedVar] = GameValue.Bool(true)
  for (effect in law.resolvedOnEnact()) {
    applyLawOnEnactEffect(config, effect)
  }
}

private fun VnEngine.lawToUi(
  law: com.olegkos.vnengine.engine.academy.AcademyLawConfig,
  config: AcademyConfig,
  dayKind: AcademyDayKind,
): EngineOutput.AcademyLawUi {
  val enacted = isLawEnacted(law)
  val resources = academyResources(config)
  val scheduleOk = law.scheduleScope().availableOn(dayKind)
  val status = when {
    enacted -> EngineOutput.AcademyLawStatus.ENACTED
    !scheduleOk -> EngineOutput.AcademyLawStatus.LOCKED
    !meetsRequires(law.requires) -> EngineOutput.AcademyLawStatus.LOCKED
    law.cost > 0 && resources < law.cost -> EngineOutput.AcademyLawStatus.LOCKED
    else -> EngineOutput.AcademyLawStatus.AVAILABLE
  }
  val lockedReason = when (status) {
    EngineOutput.AcademyLawStatus.ENACTED -> "Принят"
    EngineOutput.AcademyLawStatus.AVAILABLE -> null
    EngineOutput.AcademyLawStatus.LOCKED -> when {
      !scheduleOk -> law.scheduleScope().lockedReason(dayKind) ?: "Недоступно сегодня"
      !meetsRequires(law.requires) ->
        academyRequirementHint(law.requires, law.lockedHint, config)
      law.cost > 0 && resources < law.cost -> "Нужно ${law.cost} ресурсов"
      else -> law.lockedHint ?: "Недоступно"
    }
  }
  val requirementsText = formatRequirementsDisplay(
    requires = law.requires,
    lockedHint = law.lockedHint,
    config = config,
    resourceCost = law.cost.takeIf { it > 0 },
  )
  val descriptionText = law.description?.trim()?.takeIf { it.isNotEmpty() }
    ?: formatLawEffectSummary(law, config)
    ?: "—"
  val actionLabel = when (status) {
    EngineOutput.AcademyLawStatus.ENACTED -> "Принят"
    EngineOutput.AcademyLawStatus.AVAILABLE -> "Принять закон"
    else -> lockedReason ?: "Недоступно"
  }
  return EngineOutput.AcademyLawUi(
    id = law.id,
    label = law.label,
    status = status,
    lockedReason = lockedReason,
    cost = law.cost,
    requirementsText = requirementsText,
    descriptionText = descriptionText,
    actionLabel = actionLabel,
    actionEnabled = status == EngineOutput.AcademyLawStatus.AVAILABLE,
    effectSummary = formatLawEffectSummary(law, config),
  )
}

private fun VnEngine.formatLawEffectSummary(
  law: com.olegkos.vnengine.engine.academy.AcademyLawConfig,
  config: AcademyConfig,
): String? {
  law.effectHint?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
  val labels = lawEffectLabels(config)
  val onEnact = law.resolvedOnEnact()
  val daily = law.daily
  if (onEnact.isEmpty() && daily.isEmpty()) return null
  return buildList {
    if (onEnact.isNotEmpty()) {
      add(
        "При принятии: " + onEnact.joinToString(", ") { effect ->
          val name = labels[effect.varName] ?: effect.varName
          val isFloat = config.stats.firstOrNull { it.varName == effect.varName }?.isFloatStat() == true
          formatLawOnEnactLine(effect, name, isFloat)
        },
      )
    }
    if (daily.isNotEmpty()) {
      add(
        "Каждый день: " + daily.joinToString(", ") { effect ->
          val name = labels[effect.varName] ?: effect.varName
          formatLawDailyLine(effect, name)
        },
      )
    }
  }.joinToString("\n")
}

private fun lawEffectLabels(config: AcademyConfig): Map<String, String> {
  val fromStats = config.stats.associate { it.varName to it.label }
  return fromStats + (config.resourcesVar to config.resourcesLabel)
}

private fun VnEngine.formatRequirementsDisplay(
  requires: List<AcademyRequirementJson>,
  lockedHint: String?,
  config: AcademyConfig,
  resourceCost: Int? = null,
): String {
  val lines = mutableListOf<String>()
  when {
    !lockedHint.isNullOrBlank() -> lines.add(lockedHint)
    requires.isNotEmpty() -> {
      val labels = config.stats.associate { it.varName to it.label }
      requires.forEach { lines.add(formatAcademyRequirement(it, labels)) }
    }
    else -> lines.add("Нет особых условий")
  }
  resourceCost?.takeIf { it > 0 }?.let { lines.add("Стоимость: $it ресурсов") }
  return lines.joinToString("\n")
}

private fun VnEngine.academyRequirementHint(
  requires: List<AcademyRequirementJson>,
  lockedHint: String?,
  config: AcademyConfig,
): String {
  if (!lockedHint.isNullOrBlank()) return lockedHint
  if (requires.isEmpty()) return "Условия не выполнены"
  val labels = config.stats.associate { it.varName to it.label }
  return requires.joinToString(" · ") { formatAcademyRequirement(it, labels) }
}

private fun formatAcademyRequirement(
  req: AcademyRequirementJson,
  statLabels: Map<String, String>,
): String {
  val name = statLabels[req.variable] ?: req.variable
  val op = when (req.op.uppercase()) {
    "GTE", ">=" -> "≥"
    "LTE", "<=" -> "≤"
    "GT", ">" -> ">"
    "LT", "<" -> "<"
    "EQ", "==" -> "="
    "NEQ", "!=", "<>" -> "≠"
    else -> "≥"
  }
  val value = when (val v = req.value) {
    is com.olegkos.vnengine.engine.academy.AcademyValueJson.IntVal -> v.value.toString()
    is com.olegkos.vnengine.engine.academy.AcademyValueJson.FloatVal -> v.value.toString()
    is com.olegkos.vnengine.engine.academy.AcademyValueJson.BoolVal ->
      if (v.value) "да" else "нет"
    is com.olegkos.vnengine.engine.academy.AcademyValueJson.StringVal -> v.value
  }
  return "$name $op $value"
}

private fun VnEngine.unlockableToUi(
  unlock: com.olegkos.vnengine.engine.academy.AcademyUnlockableConfig,
  gs: AcademyState,
  dayKind: AcademyDayKind,
): EngineOutput.AcademyUnlockableUi {
  val scheduleOk = unlock.queueScheduleScope().availableOn(dayKind)
  val status = when {
    unlock.id in gs.activeUnlockIds -> EngineOutput.AcademyUnlockableStatus.ACTIVE
    unlock.id == gs.pendingUnlockId -> EngineOutput.AcademyUnlockableStatus.PENDING
    !scheduleOk -> EngineOutput.AcademyUnlockableStatus.LOCKED
    meetsRequires(unlock.unlockRequires) -> EngineOutput.AcademyUnlockableStatus.CAN_QUEUE
    else -> EngineOutput.AcademyUnlockableStatus.LOCKED
  }
  val lockedReason = when (status) {
    EngineOutput.AcademyUnlockableStatus.LOCKED -> when {
      !scheduleOk -> unlock.queueScheduleScope().lockedReason(dayKind) ?: "Недоступно сегодня"
      else -> "Условия не выполнены"
    }
    EngineOutput.AcademyUnlockableStatus.ACTIVE -> "Уже активно"
    EngineOutput.AcademyUnlockableStatus.PENDING -> "Будет активно завтра"
    EngineOutput.AcademyUnlockableStatus.CAN_QUEUE -> null
  }
  val config = state.academyConfig
  val requirementsText = if (config != null) {
    formatRequirementsDisplay(
      requires = unlock.unlockRequires,
      lockedHint = null,
      config = config,
    )
  } else {
    "—"
  }
  val descriptionText = unlock.description?.trim()?.takeIf { it.isNotEmpty() }
    ?: "Режим появится в колонках фаз дня со следующего утра."
  val actionLabel = when (status) {
    EngineOutput.AcademyUnlockableStatus.ACTIVE -> "Активно"
    EngineOutput.AcademyUnlockableStatus.PENDING -> "Отменить"
    EngineOutput.AcademyUnlockableStatus.CAN_QUEUE -> "Включить с завтра"
    else -> lockedReason ?: "Недоступно"
  }
  return EngineOutput.AcademyUnlockableUi(
    id = unlock.id,
    label = unlock.label,
    status = status,
    lockedReason = lockedReason,
    selectedForTomorrow = unlock.id == gs.pendingUnlockId,
    requirementsText = requirementsText,
    descriptionText = descriptionText,
    actionLabel = actionLabel,
    actionEnabled = status == EngineOutput.AcademyUnlockableStatus.CAN_QUEUE ||
      status == EngineOutput.AcademyUnlockableStatus.PENDING,
    completed = status == EngineOutput.AcademyUnlockableStatus.ACTIVE,
  )
}

private fun VnEngine.academyDayKind(config: AcademyConfig): AcademyDayKind =
  config.weekSchedule.dayKind(academyDay(config))

private fun VnEngine.buildingToUi(
  building: AcademyBuildingConfig,
  gs: AcademyState,
  dayKind: AcademyDayKind,
): EngineOutput.AcademyBuildingUi {
  val level = buildingLevel(building)
  val next = building.levels.filter { it.level > level }.minByOrNull { it.level }
  val config = state.academyConfig ?: return EngineOutput.AcademyBuildingUi(
    id = building.id,
    label = building.label,
    group = building.group,
    level = level,
    xPercent = building.xPercent,
    yPercent = building.yPercent,
    enabled = false,
    lockedReason = null,
    selected = false,
    statusLabel = "",
    isBuilt = level > 0,
    requirementsText = "—",
    descriptionText = "—",
    actionLabel = "Недоступно",
    completed = false,
  )
  val resources = academyResources(config)
  val currentDay = academyDay(config)
  val scheduleOk = building.buildScheduleScope().availableOn(dayKind)
  val levelOk = next == null || meetsRequires(next.requires)
  val canAfford = next == null || resources >= next.cost
  val canBuildToday = gs.hubPhase == AcademyHubPhase.PLANNING &&
    !gs.buildUsedToday &&
    next != null &&
    scheduleOk &&
    levelOk &&
    canAfford
  val statusLabel = when {
    level == 0 && next == null -> "Нет улучшений"
    level == 0 && next != null -> "Построить · ур. ${next.level}"
    next != null && levelOk && canAfford -> "Ур. $level → ${next.level}"
    next != null && !levelOk -> "Ур. $level · условия не выполнены"
    next != null && !canAfford -> "Ур. $level · мало ресурсов"
    else -> "Построено (ур. $level)"
  }
  val lockedReason = when {
    !canBuildToday && gs.buildUsedToday -> "Сегодня стройка уже запланирована"
    !canBuildToday && next == null && level > 0 -> null
    !canBuildToday && next == null -> "Недоступно"
    !canBuildToday && !scheduleOk ->
      building.buildScheduleScope().lockedReason(dayKind, "build") ?: "Недоступно сегодня"
    !canBuildToday && !levelOk -> "Условия не выполнены"
    !canBuildToday && !canAfford -> "Нужно ${next?.cost ?: 0} ресурсов"
    else -> null
  }
  val builtMaxLevel = next == null && level > 0
  val showBuildHighlight = builtMaxLevel &&
    gs.buildingHighlightDay[building.id] == currentDay
  val requirementsText = when {
    next != null -> formatRequirementsDisplay(
      requires = next.requires,
      lockedHint = null,
      config = config,
      resourceCost = next.cost.takeIf { it > 0 },
    )
    level > 0 -> "Построено (уровень $level)"
    else -> "Нет доступных улучшений"
  }
  val descriptionText = next?.description?.trim()?.takeIf { it.isNotEmpty() }
    ?: if (builtMaxLevel) "Все улучшения этого строения уже возведены."
    else "—"
  val actionLabel = when {
    builtMaxLevel -> "Построено"
    gs.selectedBuildingId == building.id -> "Снять выбор"
    canBuildToday -> "Построить"
    else -> lockedReason ?: "Недоступно"
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
    buildCost = next?.cost?.takeIf { it > 0 },
    requirementsText = requirementsText,
    descriptionText = descriptionText,
    actionLabel = actionLabel,
    completed = showBuildHighlight,
  )
}

private fun groupLabel(groupId: String): String = when (groupId) {
  "study" -> "Учёба"
  "life" -> "Быт"
  else -> groupId.replaceFirstChar { it.uppercase() }
}

fun VnEngine.currentAcademyHubNode(): SceneNode.AcademyHub? =
  currentNode() as? SceneNode.AcademyHub
