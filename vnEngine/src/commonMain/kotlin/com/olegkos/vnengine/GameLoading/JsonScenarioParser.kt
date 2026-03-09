package com.olegkos.vnengine.GameLoading

import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.SceneNode.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.*


class JsonScenarioParser : ScenarioParser {

  override fun parse(raw: String): Scenario {

    val json = Json {
      ignoreUnknownKeys = true
      classDiscriminator = "type"
    }

    val parsed = json.decodeFromString<ScenarioJson>(raw)

    val scenes: Map<String, Scene> =
      parsed.scenes.mapValues { (sceneId, sceneJson) ->

        Scene(
          id = sceneId,
          nodes = sceneJson.nodes.map { nodeJson ->

            when (nodeJson) {

              is SceneNodeJson.Text ->
                Text(nodeJson.text)

              is SceneNodeJson.Choice ->
                Choice(
                  options = nodeJson.options.map {
                    Option(
                      text = it.text,
                      nextSceneId = it.nextSceneId
                    )
                  }
                )
              is SceneNodeJson.SetVar ->
                SetVar(nodeJson.varName, nodeJson.value.toGameValue())

              is SceneNodeJson.ModifyVar ->
                ModifyVar(nodeJson.varName, nodeJson.value.toGameValue())

              is SceneNodeJson.DiceRoll ->
                DiceRoll(
                  name = nodeJson.name,
                  sides = nodeJson.sides,
                  modifierVar = nodeJson.modifierVar,
                  difficulty = nodeJson.difficulty,
                  successScene = nodeJson.successScene,
                  failScene = nodeJson.failScene,
                  critSuccessScene = nodeJson.critSuccessScene,
                  critFailScene = nodeJson.critFailScene
                )

              is SceneNodeJson.If -> If(
                variable = nodeJson.variable,
                equals = nodeJson.equals.toGameValue(),
                successScene = nodeJson.successScene,
                failScene = nodeJson.failScene
              )

              is JumpScenario -> SceneNode.JumpScenario(
                scenarioFile = nodeJson.scenarioFile
              )
            }
          }
        )
      }
    return Scenario(
      startSceneId = parsed.startSceneId,
      scenes = scenes
    )
  }
}

@Serializable
data class ScenarioJson(
  val startSceneId: String,
  val scenes: Map<String, SceneJson>
)

@Serializable
data class SceneJson(
  val nodes: List<SceneNodeJson>
)
@Serializable
@SerialName("jumpScenario")
data class JumpScenario(
  val scenarioFile: String
) : SceneNodeJson()
@Serializable
sealed class SceneNodeJson {

  @Serializable
  @SerialName("text")
  data class Text(
    val text: String
  ) : SceneNodeJson()

  @Serializable
  @SerialName("choice")
  data class Choice(
    val options: List<OptionJson>
  ) : SceneNodeJson()


  @Serializable
  @SerialName("set")
  data class SetVar(val varName: String, val value: GameValueJson) : SceneNodeJson()

  @Serializable
  @SerialName("modify")
  data class ModifyVar(val varName: String, val value: GameValueJson) : SceneNodeJson()
  @Serializable
  @SerialName("if")
  data class If(
    val variable: String,
    val equals: GameValueJson,
    val successScene: String,
    val failScene: String
  ) : SceneNodeJson()
  @Serializable
  @SerialName("dice")
  data class DiceRoll(
    val name: String,
    val sides: Int,
    val modifierVar: String,
    val difficulty: Int,
    val successScene: String,
    val failScene: String,
    val critSuccessScene: String? = null,
    val critFailScene: String? = null
  ) : SceneNodeJson()
}
@Serializable
sealed class GameValueJson {

  @Serializable
  @SerialName("int")
  data class IntVal(val value: Int) : GameValueJson()

  @Serializable
  @SerialName("float")
  data class FloatVal(val value: Float) : GameValueJson()

  @Serializable
  @SerialName("bool")
  data class BoolVal(val value: Boolean) : GameValueJson()

  @Serializable
  @SerialName("string")
  data class StringVal(val value: String) : GameValueJson()

  fun toGameValue(): com.olegkos.vnengine.engine.variables.GameValue = when (this) {
    is IntVal -> com.olegkos.vnengine.engine.variables.GameValue.IntVal(value)
    is FloatVal -> com.olegkos.vnengine.engine.variables.GameValue.FloatVal(value)
    is BoolVal -> com.olegkos.vnengine.engine.variables.GameValue.Bool(value)
    is StringVal -> com.olegkos.vnengine.engine.variables.GameValue.StringVal(value)
  }
}
@Serializable
data class OptionJson(
  val text: String,
  val nextSceneId: String
)