package com.olegkos.virtualnoveltesttwo.GameLoading

import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
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
                SceneNode.Text(nodeJson.text)

              is SceneNodeJson.Choice ->
                SceneNode.Choice(
                  options = nodeJson.options.map {
                    Option(
                      text = it.text,
                      nextSceneId = it.nextSceneId
                    )
                  }
                )

              is SceneNodeJson.DiceRoll ->
                SceneNode.DiceRoll(
                  name = nodeJson.name,
                  sides = nodeJson.sides,
                  modifier = nodeJson.modifier,
                  difficulty = nodeJson.difficulty,
                  successScene = nodeJson.successScene,
                  failScene = nodeJson.failScene,
                  critSuccessScene = nodeJson.critSuccessScene,
                  critFailScene = nodeJson.critFailScene
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
sealed class SceneNodeJson {

  @kotlinx.serialization.Serializable
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
  @SerialName("dice")
  data class DiceRoll(
    val name: String,
    val sides: Int,
    val modifier: Int,
    val difficulty: Int,
    val successScene: String,
    val failScene: String,
    val critSuccessScene: String? = null,
    val critFailScene: String? = null
  ) : SceneNodeJson()
}

@Serializable
data class OptionJson(
  val text: String,
  val nextSceneId: String
)