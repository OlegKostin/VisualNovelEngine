package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.NodePointer
import com.olegkos.vnengine.engine.EngineOutput.*
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.Option

class VnEngine(
  val state: GameState,
  private val dice: DiceRoller
) {

  private val scenes = mutableMapOf<String, Scene>()

  fun addScene(id: String, scene: Scene) {
    scenes[id] = scene
  }

  fun addScenes(newScenes: Map<String, Scene>) {
    scenes.putAll(newScenes)
  }

  fun currentNode(): SceneNode {

    val scene = scenes[state.pointer.sceneId]
      ?: error("Scene not found")

    return scene.nodes
      .getOrNull(state.pointer.nodeIndex)
      ?: error("Node not found")
  }

  private tailrec fun resolveNode(): SceneNode {

    val node = currentNode()

    if (node is SceneNode.Jump) {
      jumpToScene(node.targetSceneId)
      return resolveNode()
    }

    return node
  }

  fun next(selectedOption: Option? = null) {

    val node = resolveNode()

    when (node) {

      is SceneNode.Text -> {
        // просто идём к следующему узлу
        state.pointer = state.pointer.copy(
          nodeIndex = state.pointer.nodeIndex + 1
        )
      }

      is SceneNode.Choice -> {
        selectedOption?.let {
          jumpToScene(it.nextSceneId)
        }
      }

      is SceneNode.DiceRoll -> {
        if (state.diceResult == null) {
          // бросок ещё не сделан
          state.diceResult = dice.roll(node.sides)
          return
        }

        val roll = state.diceResult!!
        val total = roll + node.modifier

        when {
          // критическая неудача
          roll == 1 && node.critFailScene != null ->
            jumpToScene(node.critFailScene)

          // критический успех
          roll == node.sides && node.critSuccessScene != null ->
            jumpToScene(node.critSuccessScene)

          // обычный успех
          total >= node.difficulty ->
            jumpToScene(node.successScene)

          else ->
            jumpToScene(node.failScene)
        }
      }

      is SceneNode.Jump -> error("Jump should never reach next()")
    }
  }
  fun jumpToScene(sceneId: String) {

    require(scenes.containsKey(sceneId)) {
      "Scene '$sceneId' not found"
    }

    state.pointer = NodePointer(sceneId, 0)
    state.diceResult = null
  }

  fun currentOutput(): EngineOutput =
    when (val node = resolveNode()) {

      is SceneNode.Text ->
        ShowText(node.text)

      is SceneNode.Choice ->
        ShowChoices(node.options)

      is SceneNode.DiceRoll ->
        ShowDice(
          name = node.name,
          sides = node.sides,
          result = state.diceResult,
          modifier = node.modifier,
          difficulty = node.difficulty
        )

      is SceneNode.Jump -> error("Jump should never reach output")
    }}