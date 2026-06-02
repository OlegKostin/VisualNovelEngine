package com.olegkos.vnengine.engine

import com.olegkos.vnengine.game.GameOverConfig

data class GlobalGameOverTarget(
  val sceneId: String,
)

/**
 * Вне боя: HP или рассудок ≤ 0 → сцена game over из [GameOverConfig].
 * В бою поражение по-прежнему через [SceneNode.Battle.transitions.loseScene].
 */
fun VnEngine.resolveGlobalGameOverTarget(config: GameOverConfig): GlobalGameOverTarget? {
  if (!config.isConfigured()) return null
  if (!state.isGameInitialized) return null
  if (state.battle != null) return null
  if (state.diceDuel != null) return null
  if (state.cardGame != null) return null
  if (config.isGameOverScene(state.pointer.sceneId)) return null

  val health = variables.getModifier(config.healthVar).toInt()
  val sanity = variables.getModifier(config.sanityVar).toInt()

  val sceneId = when {
    health <= 0 -> config.sceneForHealth()
    sanity <= 0 -> config.sceneForSanity()
    else -> null
  } ?: return null

  return GlobalGameOverTarget(sceneId)
}

fun VnEngine.clearMinigamesForGameOver() {
  state.battle = null
  state.diceDuel = null
  state.cardGame = null
  state.pendingDiceJumpScene = null
}
