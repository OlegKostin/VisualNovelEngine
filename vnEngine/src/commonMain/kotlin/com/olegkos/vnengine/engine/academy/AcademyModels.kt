package com.olegkos.vnengine.engine.academy

import com.olegkos.vnengine.engine.NodePointer
import com.olegkos.vnengine.scene.SceneNode
import kotlinx.serialization.Serializable

@Serializable
data class AcademyConfig(
  val background: String,
  val dayVar: String = "academy_day",
  val resourcesVar: String = "resources",
  val resourcesLabel: String = "Ресурсы",
  /** Показатели академии: переменные из variables.json, отображаются в хабе и в итогах дня. */
  val stats: List<AcademyStatConfig> = emptyList(),
  val phases: List<AcademyPhaseConfig> = defaultPhases,
  val buildings: List<AcademyBuildingConfig> = emptyList(),
  val activities: List<AcademyActivityConfig> = emptyList(),
  /** Разблокируемые режимы: выбор в меню «Построить» → активны со следующего дня. */
  val unlockableActions: List<AcademyUnlockableConfig> = emptyList(),
  /** Одноразовые законы: ресурсы, сценарий, флаг enactedVar = true, зелёная строка в меню. */
  val laws: List<AcademyLawConfig> = emptyList(),
  val randomEvents: List<AcademyRandomEventConfig> = emptyList(),
  /** 5 будних + 2 выходных в цикле (день академии с 1). */
  val weekSchedule: AcademyWeekSchedule = AcademyWeekSchedule(),
) {
  companion object {
    val defaultPhases = listOf(
      AcademyPhaseConfig("morning", "Утро"),
      AcademyPhaseConfig("day", "День"),
      AcademyPhaseConfig("evening", "Вечер"),
      AcademyPhaseConfig("night", "Ночь"),
    )
  }
}

@Serializable
data class AcademyStatConfig(
  val varName: String,
  val label: String,
  /** "int" — целое; "float" — дробное (например лояльность). */
  val type: String = "int",
)

@Serializable
data class AcademyPhaseConfig(
  val id: String,
  val label: String,
)

@Serializable
data class AcademyBuildingConfig(
  val id: String,
  val label: String,
  val group: String,
  val levelVar: String,
  val xPercent: Float = 50f,
  val yPercent: Float = 70f,
  /** Когда можно строить/улучшать: always | weekday | weekend */
  val buildSchedule: String? = null,
  /** Когда можно посещать (действия здания), если у действия нет своего schedule */
  val visitSchedule: String? = null,
  /** @deprecated то же, что [buildSchedule] */
  val schedule: String? = null,
  val levels: List<AcademyBuildingLevelConfig> = emptyList(),
) {
  fun buildScheduleScope(): AcademyScheduleScope =
    AcademyScheduleScope.fromJson(buildSchedule ?: schedule)

  fun visitScheduleScope(): AcademyScheduleScope =
    AcademyScheduleScope.fromJson(visitSchedule)
}

@Serializable
data class AcademyBuildingLevelConfig(
  val level: Int,
  val scenarioFile: String,
  val cost: Int = 0,
  val description: String? = null,
  val requires: List<AcademyRequirementJson> = emptyList(),
  /** Действия в хабе после этого уровня; при апгрейде заменяют список предыдущего уровня. */
  val activities: List<AcademyActivityConfig> = emptyList(),
)

@Serializable
data class AcademyLawConfig(
  val id: String,
  val label: String,
  /** Bool-переменная: true после принятия закона. */
  val enactedVar: String,
  val cost: Int = 0,
  val requires: List<AcademyRequirementJson> = emptyList(),
  val lockedHint: String? = null,
  val scenarioFile: String? = null,
  val description: String? = null,
  /** @deprecated используйте [onEnact] */
  val effects: List<AcademyLawOnEnactEffectJson> = emptyList(),
  /** Одноразово при принятии закона (целые дельты). */
  val onEnact: List<AcademyLawOnEnactEffectJson> = emptyList(),
  /** Каждый день, пока закон принят (дробный случайный диапазон). */
  val daily: List<AcademyLawDailyEffectJson> = emptyList(),
  /** Свой текст эффекта; если пусто — собирается из onEnact и daily. */
  val effectHint: String? = null,
  /** always | weekday | weekend */
  val schedule: String? = null,
) {
  fun scheduleScope(): AcademyScheduleScope = AcademyScheduleScope.fromJson(schedule)
}

@Serializable
data class AcademyLawOnEnactEffectJson(
  val varName: String,
  val delta: Int = 0,
)

@Serializable
data class AcademyLawDailyEffectJson(
  val varName: String,
  val deltaMin: Float,
  val deltaMax: Float,
)

/** @deprecated typealias для старых JSON с "effects" */
typealias AcademyLawEffectJson = AcademyLawOnEnactEffectJson

@Serializable
data class AcademyUnlockableConfig(
  val id: String,
  val label: String,
  val description: String? = null,
  val unlockRequires: List<AcademyRequirementJson> = emptyList(),
  val activities: List<AcademyActivityConfig> = emptyList(),
  /** Когда можно включить режим в меню */
  val queueSchedule: String? = null,
  /** Расписание посещения для [activities], если у действия нет своего schedule */
  val visitSchedule: String? = null,
  /** @deprecated то же, что [queueSchedule] */
  val schedule: String? = null,
) {
  fun queueScheduleScope(): AcademyScheduleScope =
    AcademyScheduleScope.fromJson(queueSchedule ?: schedule)

  fun visitScheduleScope(): AcademyScheduleScope =
    AcademyScheduleScope.fromJson(visitSchedule)
}

@Serializable
data class AcademyActivityConfig(
  val id: String,
  val label: String,
  val scenarioFile: String,
  val phases: List<String> = emptyList(),
  val requires: List<AcademyRequirementJson> = emptyList(),
  /** Когда можно выбрать в фазах дня (посещение): always | weekday | weekend */
  val schedule: String? = null,
) {
  fun scheduleScope(): AcademyScheduleScope = AcademyScheduleScope.fromJson(schedule)

  /** schedule действия → visitSchedule здания → visitSchedule разблокировки → always */
  fun visitScopeFor(
    building: AcademyBuildingConfig? = null,
    unlock: AcademyUnlockableConfig? = null,
  ): AcademyScheduleScope {
    if (!schedule.isNullOrBlank()) return scheduleScope()
    building?.visitSchedule?.takeIf { it.isNotBlank() }?.let { return building.visitScheduleScope() }
    unlock?.visitSchedule?.takeIf { it.isNotBlank() }?.let { return unlock.visitScheduleScope() }
    return AcademyScheduleScope.ALWAYS
  }
}

@Serializable
data class AcademyRandomEventConfig(
  val id: String,
  val scenarioFile: String,
  val chance: Double = 0.25,
  val afterPhase: String,
  val weight: Int = 1,
  val requires: List<AcademyRequirementJson> = emptyList(),
)

@Serializable
data class AcademyRequirementJson(
  val variable: String,
  val op: String = "GTE",
  val value: AcademyValueJson,
)

@Serializable
sealed class AcademyValueJson {
  @Serializable
  @kotlinx.serialization.SerialName("int")
  data class IntVal(val value: Int) : AcademyValueJson()

  @Serializable
  @kotlinx.serialization.SerialName("float")
  data class FloatVal(val value: Float) : AcademyValueJson()

  @Serializable
  @kotlinx.serialization.SerialName("bool")
  data class BoolVal(val value: Boolean) : AcademyValueJson()

  @Serializable
  @kotlinx.serialization.SerialName("string")
  data class StringVal(val value: String) : AcademyValueJson()
}

fun AcademyRequirementJson.toRequirement(): SceneNode.WeightedRandomJump.Requirement =
  SceneNode.WeightedRandomJump.Requirement(
    variable = variable,
    op = op.toWeightedOp(),
    value = value.toGameValue(),
  )

private fun String.toWeightedOp(): SceneNode.WeightedRandomJump.Op =
  when (uppercase()) {
    "EQ", "==" -> SceneNode.WeightedRandomJump.Op.EQ
    "NEQ", "!=", "<>" -> SceneNode.WeightedRandomJump.Op.NEQ
    "GTE", ">=" -> SceneNode.WeightedRandomJump.Op.GTE
    "LTE", "<=" -> SceneNode.WeightedRandomJump.Op.LTE
    "GT", ">" -> SceneNode.WeightedRandomJump.Op.GT
    "LT", "<" -> SceneNode.WeightedRandomJump.Op.LT
    else -> SceneNode.WeightedRandomJump.Op.GTE
  }

private fun AcademyValueJson.toGameValue(): com.olegkos.vnengine.engine.variables.GameValue =
  when (this) {
    is AcademyValueJson.IntVal -> com.olegkos.vnengine.engine.variables.GameValue.IntVal(value)
    is AcademyValueJson.FloatVal -> com.olegkos.vnengine.engine.variables.GameValue.FloatVal(value)
    is AcademyValueJson.BoolVal -> com.olegkos.vnengine.engine.variables.GameValue.Bool(value)
    is AcademyValueJson.StringVal -> com.olegkos.vnengine.engine.variables.GameValue.StringVal(value)
  }

enum class AcademyHubPhase {
  PLANNING,
  PLAYBACK,
}

/** Один шаг проигрывания дня; отдельный шаг даже при том же scenarioFile. */
data class AcademyPlaybackStep(
  val scenarioFile: String,
  val phaseId: String = "",
  val phaseLabel: String = "",
  val activityLabel: String = "",
  /** После сценария стройки — id здания и целевой уровень. */
  val buildingId: String? = null,
  val upgradeToLevel: Int? = null,
  val isDaySummary: Boolean = false,
)

data class AcademyState(
  val configPath: String,
  var hubPhase: AcademyHubPhase = AcademyHubPhase.PLANNING,
  var selectedBuildingId: String? = null,
  val planByPhase: MutableMap<String, String> = mutableMapOf(),
  var playbackQueue: List<AcademyPlaybackStep> = emptyList(),
  var playbackIndex: Int = 0,
  /** Сценарий с узлом academyHub (куда вернуться после дня). */
  var returnScenario: String? = null,
  var returnPointer: NodePointer? = null,
  var buildUsedToday: Boolean = false,
  var randomEventId: String? = null,
  /** Значения переменных академии на момент «Подтвердить день». */
  val dayStartVars: MutableMap<String, Float> = mutableMapOf(),
  val activeUnlockIds: MutableSet<String> = mutableSetOf(),
  var pendingUnlockId: String? = null,
  /** Закон, сценарий которого сейчас проигрывается (до EndOfScene). */
  var pendingLawEnactId: String? = null,
  var lawReturnScenario: String? = null,
  var lawReturnPointer: NodePointer? = null,
  val buildingsBuiltThisPlayback: MutableSet<String> = mutableSetOf(),
  val buildingHighlightDay: MutableMap<String, Int> = mutableMapOf(),
)
