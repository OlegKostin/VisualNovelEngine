package com.olegkos.vnengine.engine

import com.olegkos.vnengine.engine.EngineOutput.ShowTargetTap
import com.olegkos.vnengine.engine.EngineOutput.TargetTapTargetUi
import com.olegkos.vnengine.engine.targettap.TargetTapActiveTarget
import com.olegkos.vnengine.engine.targettap.TargetTapState
import com.olegkos.vnengine.scene.SceneNode
import kotlin.random.Random

internal fun VnEngine.handleTargetTapNode(node: SceneNode.TargetTap): EngineOutput {
  val ts = state.targetTap ?: TargetTapState(gameId = node.id).also { state.targetTap = it }

  if (node.autoStart && !ts.started) {
    beginTargetTapRound(node, ts)
  } else if (ts.started && !ts.awaitingSpawn && ts.activeTargets.isEmpty() && canSpawnMoreTargets(ts, node)) {
    fillTargetTapSpawns(node, ts)
  }

  return buildTargetTapOutput(node, ts)
}

fun VnEngine.processTargetTapStart(): EngineOutput {
  val node = currentTargetTapNode() ?: return tick()
  val ts = state.targetTap ?: return tick()
  if (ts.started) {
    return buildTargetTapOutput(node, ts)
  }
  beginTargetTapRound(node, ts)
  return buildTargetTapOutput(node, ts)
}

fun VnEngine.processTargetTapHit(targetId: String): EngineOutput {
  val node = currentTargetTapNode() ?: return tick()
  val ts = state.targetTap ?: return tick()
  if (!ts.started) return buildTargetTapOutput(node, ts)
  if (!ts.activeTargets.removeAll { it.id == targetId }) {
    return buildTargetTapOutput(node, ts)
  }

  ts.caughtTotal++
  if (ts.caughtTotal >= node.targetCount) {
    return finishTargetTap(node, success = true)
  }

  ts.awaitingSpawn = true
  return buildTargetTapOutput(node, ts)
}

fun VnEngine.processTargetTapMiss(targetId: String): EngineOutput {
  val node = currentTargetTapNode() ?: return tick()
  val ts = state.targetTap ?: return tick()
  if (!ts.started) return buildTargetTapOutput(node, ts)
  if (!ts.activeTargets.removeAll { it.id == targetId }) {
    return buildTargetTapOutput(node, ts)
  }

  ts.missCount++
  if (ts.missCount >= node.maxMisses) {
    return finishTargetTap(node, success = false)
  }

  if (ts.caughtTotal >= node.targetCount) {
    return finishTargetTap(node, success = true)
  }

  ts.awaitingSpawn = true
  return buildTargetTapOutput(node, ts)
}

fun VnEngine.processTargetTapContinueSpawn(): EngineOutput {
  val node = currentTargetTapNode() ?: return tick()
  val ts = state.targetTap ?: return tick()
  if (!ts.started) return buildTargetTapOutput(node, ts)
  ts.awaitingSpawn = false
  if (canSpawnMoreTargets(ts, node)) {
    fillTargetTapSpawns(node, ts)
  }
  return buildTargetTapOutput(node, ts)
}

fun VnEngine.currentTargetTapNode(): SceneNode.TargetTap? =
  currentNode() as? SceneNode.TargetTap

private fun VnEngine.beginTargetTapRound(node: SceneNode.TargetTap, ts: TargetTapState) {
  ts.started = true
  ts.awaitingSpawn = false
  ts.activeTargets.clear()
  if (canSpawnMoreTargets(ts, node)) {
    fillTargetTapSpawns(node, ts)
  }
}

private fun VnEngine.finishTargetTap(
  node: SceneNode.TargetTap,
  success: Boolean,
): EngineOutput {
  val sceneId = if (success) node.successScene else node.failScene
  state.targetTap = null
  jumpToScene(sceneId)
  return tick()
}

private fun canSpawnMoreTargets(ts: TargetTapState, node: SceneNode.TargetTap): Boolean {
  if (!ts.started || ts.caughtTotal >= node.targetCount) return false
  return ts.caughtTotal + ts.activeTargets.size < node.targetCount
}

private fun VnEngine.fillTargetTapSpawns(node: SceneNode.TargetTap, ts: TargetTapState) {
  while (
    ts.activeTargets.size < node.simultaneous &&
    canSpawnMoreTargets(ts, node)
  ) {
    ts.activeTargets.add(createTargetTapTarget(node, ts))
    ts.spawnedTotal++
  }
}

private fun VnEngine.createTargetTapTarget(
  node: SceneNode.TargetTap,
  ts: TargetTapState,
): TargetTapActiveTarget {
  val image = node.images.random()
  val (x, y) = pickTargetTapPosition(ts.activeTargets)
  val id = "${node.id}_${ts.targetSeq++}"
  return TargetTapActiveTarget(id = id, image = image, xPercent = x, yPercent = y)
}

private fun pickTargetTapPosition(existing: List<TargetTapActiveTarget>): Pair<Float, Float> {
  repeat(24) {
    val x = Random.nextFloat() * 70f + 15f
    val y = Random.nextFloat() * 70f + 15f
    if (existing.none { other ->
        val dx = other.xPercent - x
        val dy = other.yPercent - y
        dx * dx + dy * dy < 12f * 12f
      }
    ) {
      return x to y
    }
  }
  return Random.nextFloat() * 70f + 15f to Random.nextFloat() * 70f + 15f
}

private fun VnEngine.buildTargetTapOutput(
  node: SceneNode.TargetTap,
  ts: TargetTapState,
): ShowTargetTap {
  val prompt = node.prompt?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables)
  val startPrompt = node.startPrompt?.trim()?.takeIf { it.isNotEmpty() }?.let(::resolveTextVariables)
    ?: DEFAULT_TARGET_TAP_START_PROMPT
  val modifierBonus = node.modifierVar
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { variables.getCheckModifier(it) }
    ?: 0f
  val lifetimeMs = node.lifetimeMs +
    (modifierBonus * node.modifierLifetimeMsPerPoint).toLong()

  return ShowTargetTap(
    gameId = node.id,
    overlayDarkness = node.overlayDarkness,
    prompt = prompt,
    lifetimeMs = lifetimeMs.coerceAtLeast(400L),
    startScale = node.startScale,
    endScale = node.endScale,
    hitRadiusPercent = node.hitRadiusPercent,
    spawnDelayMs = node.spawnDelayMs,
    targetCount = node.targetCount,
    caughtCount = ts.caughtTotal,
    missCount = ts.missCount,
    maxMisses = node.maxMisses,
    awaitingSpawn = ts.awaitingSpawn,
    started = ts.started,
    startPrompt = startPrompt,
    activeTargets = ts.activeTargets.map {
      TargetTapTargetUi(
        id = it.id,
        image = it.image,
        xPercent = it.xPercent,
        yPercent = it.yPercent,
      )
    },
  )
}

private const val DEFAULT_TARGET_TAP_START_PROMPT = "Нажми, чтобы начать"
