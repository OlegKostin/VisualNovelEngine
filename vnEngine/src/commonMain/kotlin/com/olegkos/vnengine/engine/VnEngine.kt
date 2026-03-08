package com.olegkos.vnengine.engine

import com.olegkos.vnengine.GameLoading.DiceRoller
import com.olegkos.vnengine.GameLoading.NodePointer
import com.olegkos.vnengine.engine.EngineOutput.ShowChoices
import com.olegkos.vnengine.engine.EngineOutput.ShowDice
import com.olegkos.vnengine.engine.EngineOutput.ShowText
import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.engine.variables.VariableStore
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode

class VnEngine(
  val state: GameState,
  private val dice: DiceRoller
) {
  val variables = VariableStore(state.variables)
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
        state.pointer = state.pointer.copy(
          nodeIndex = state.pointer.nodeIndex + 1
        )
      }

      is SceneNode.Choice -> {
        selectedOption?.let {
          jumpToScene(it.nextSceneId)
        }
      }

      is SceneNode.SetVar -> {
        state.variables[node.varName] = node.value
        state.pointer = state.pointer.copy(nodeIndex = state.pointer.nodeIndex + 1)
      }

      is SceneNode.ModifyVar -> {
        val old = state.variables[node.varName]
        state.variables[node.varName] = when {
          old is GameValue.IntVal && node.value is GameValue.IntVal ->
            GameValue.IntVal(old.value + node.value.value)
          old is GameValue.FloatVal && node.value is GameValue.FloatVal ->
            GameValue.FloatVal(old.value + node.value.value)
          else -> node.value
        }
        state.pointer = state.pointer.copy(nodeIndex = state.pointer.nodeIndex + 1)
      }

      is SceneNode.DiceRoll -> {
        if (state.diceResult == null) {
          state.diceResult = dice.roll(node.sides)
          return
        }

        val roll = state.diceResult!!
        val mod = variables.getInt(node.modifierVar)
        val total = roll + mod

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

      is SceneNode.DiceRoll -> {
        val mod = variables.getInt(node.modifierVar)
        ShowDice(
          name = node.name,
          sides = node.sides,
          result = state.diceResult,
          modifier = mod,
          difficulty = node.difficulty
        )
      }
      is SceneNode.SetVar,
      is SceneNode.ModifyVar -> {
        next()
        currentOutput()
      }
      is SceneNode.Jump -> error("Jump should never reach output")
    }}