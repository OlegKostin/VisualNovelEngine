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
  val phases: List<AcademyPhaseConfig> = defaultPhases,
  val buildings: List<AcademyBuildingConfig> = emptyList(),
  val activities: List<AcademyActivityConfig> = emptyList(),
  /** Разблокируемые режимы: выбор в меню «Построить» → активны со следующего дня. */
  val unlockableActions: List<AcademyUnlockableConfig> = emptyList(),
  val randomEvents: List<AcademyRandomEventConfig> = emptyList(),
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
  val levels: List<AcademyBuildingLevelConfig> = emptyList(),
)

@Serializable
data class AcademyBuildingLevelConfig(
  val level: Int,
  val scenarioFile: String,
  val cost: Int = 0,
  val requires: List<AcademyRequirementJson> = emptyList(),
  /** Действия в колонках фаз дня после достижения этого уровня постройки. */
  val activities: List<AcademyActivityConfig> = emptyList(),
)

@Serializable
data class AcademyUnlockableConfig(
  val id: String,
  val label: String,
  val unlockRequires: List<AcademyRequirementJson> = emptyList(),
  val activities: List<AcademyActivityConfig> = emptyList(),
)

@Serializable
data class AcademyActivityConfig(
  val id: String,
  val label: String,
  val scenarioFile: String,
  val phases: List<String> = emptyList(),
  val requires: List<AcademyRequirementJson> = emptyList(),
)

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
  val dayStartVars: MutableMap<String, Int> = mutableMapOf(),
  val activeUnlockIds: MutableSet<String> = mutableSetOf(),
  var pendingUnlockId: String? = null,
)
