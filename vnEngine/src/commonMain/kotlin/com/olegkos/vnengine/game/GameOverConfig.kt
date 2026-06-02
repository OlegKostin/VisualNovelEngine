package com.olegkos.vnengine.game

import kotlinx.serialization.Serializable

@Serializable
data class GameOverConfig(
  val healthVar: String = "health",
  val sanityVar: String = "mental_health",
  /** Сцена по умолчанию, если не заданы [healthScene] / [sanityScene] */
  val scene: String? = null,
  val healthScene: String? = null,
  val sanityScene: String? = null,
  /**
   * Опционально: подгрузить сценарий перед переходом (сцены game over в отдельном JSON).
   * Путь относительно папки game/, как [GameConfig.startScenario].
   */
  val scenarioFile: String? = null,
) {
  fun sceneForHealth(): String? = healthScene ?: scene

  fun sceneForSanity(): String? = sanityScene ?: scene

  fun isConfigured(): Boolean = sceneForHealth() != null || sceneForSanity() != null

  fun allGameOverSceneIds(): Set<String> = buildSet {
    scene?.let { add(it) }
    healthScene?.let { add(it) }
    sanityScene?.let { add(it) }
  }

  fun isGameOverScene(sceneId: String): Boolean = sceneId in allGameOverSceneIds()
}
