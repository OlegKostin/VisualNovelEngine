package com.olegkos.vnengine.GameLoading

import com.olegkos.vnengine.engine.variables.GameValue
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.SceneNode.*
import com.olegkos.vnengine.scene.SceneNode.Background
import com.olegkos.vnengine.scene.SceneNode.Choice
import com.olegkos.vnengine.scene.SceneNode.DiceRoll
import com.olegkos.vnengine.scene.SceneNode.If
import com.olegkos.vnengine.scene.SceneNode.Image
import com.olegkos.vnengine.scene.SceneNode.Jump
import com.olegkos.vnengine.scene.SceneNode.JumpScenario
import com.olegkos.vnengine.scene.SceneNode.ModifyVar
import com.olegkos.vnengine.scene.SceneNode.SetVar
import com.olegkos.vnengine.scene.SceneNode.Switch
import com.olegkos.vnengine.scene.SceneNode.Text
import com.olegkos.vnengine.scene.SubClass
import com.olegkos.vnengine.scene.SubClass.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


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

            println("Node ${nodeJson::class.simpleName} -> $nodeJson")
            when (nodeJson) {

              is SceneNodeJson.Text ->
                Text(speaker = nodeJson.speaker,
                  speakerVar = nodeJson.speakerVar,
                  text = nodeJson.text)

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
                ModifyVar(nodeJson.varName, nodeJson.value.toGameValue(),nodeJson.text)

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

              is JumpScenarioJson -> JumpScenario(
                scenarioFile = nodeJson.scenarioFile
              )
              is BackgroundNode -> Background(nodeJson.image)
              is ImageNode -> Image(nodeJson.image)
              is EffectNode -> Image(nodeJson.image)
              is SceneNodeJson.JumpJson -> Jump(
                targetSceneId = nodeJson.nextSceneId
              )

              is SceneNodeJson.Switch -> Switch(
                variable = nodeJson.variable,
                cases = nodeJson.cases,
                default = nodeJson.default
              )

              is SceneNodeJson.SwitchRange -> SwitchRange(
                variable = nodeJson.variable,
                ranges = nodeJson.ranges.map {
                  RangeCase(it.min, it.max, it.scene)
                },
                default = nodeJson.default
              )

              is CharacterNode -> ShowCharacter(
                id = nodeJson.id,
                image = nodeJson.image,
                flagVar = nodeJson.flagVar,
                trueImage = nodeJson.trueImage,
                falseImage = nodeJson.falseImage,
                position = nodeJson.position,
                scale = nodeJson.scale,
              )

              is CharacterHideNode -> HideCharacter(
                id = nodeJson.id
              )

              is InitGameNode -> InitGame(
                playerNameVar = nodeJson.playerNameVar,
                classVar = nodeJson.classVar,
                classes = nodeJson.classes.map {
                  GameClass(
                    id = it.id,
                    name = it.name,
                    stats = it.stats
                  )
                },
                nextSceneId = nodeJson.nextSceneId
              )

              is HideImageNode -> HideImage
              is SceneViewNode -> SceneView(
                background = nodeJson.background,
                navigation = nodeJson.navigation?.let {
                  Navigation(
                    up = it.up,
                    down = it.down,
                    left = it.left,
                    right = it.right
                  )
                },
                hotspots = nodeJson.hotspots.map {
                  Hotspot(
                    xPercent = it.xPercent,
                    yPercent = it.yPercent,
                    widthPercent = it.widthPercent,
                    heightPercent = it.heightPercent,
                    targetScenarioFile = it.targetScenarioFile
                  )
                }
              )

              is SceneNodeJson.DrawCard -> DrawCard(
                random = nodeJson.random,
                value = nodeJson.value,
                image = nodeJson.image
              )

              is SceneNodeJson.BattleNode -> Battle(
                id = nodeJson.id,
                title = nodeJson.title,
                monster = SceneNode.Monster(
                  name = nodeJson.monster.name,
                  image = nodeJson.monster.image,
                  health = nodeJson.monster.health,
                  horrorDamage = nodeJson.monster.horrorDamage,
                  combatDamage = nodeJson.monster.combatDamage
                ),
                player = SceneNode.PlayerRefs(
                  healthVar = nodeJson.player.healthVar,
                  sanityVar = nodeJson.player.sanityVar
                ),
                phases = SceneNode.BattlePhases(
                  horror = nodeJson.phases.horror?.let {
                    SceneNode.CheckPhase(
                      enabled = it.enabled,
                      name = it.name,
                      sides = it.sides,
                      difficulty = it.difficulty,
                      modifierVar = it.modifierVar,
                      onFailSanityDamage = it.onFailSanityDamage
                    )
                  },
                  combat = SceneNode.CombatPhase(
                    name = nodeJson.phases.combat.name,
                    sides = nodeJson.phases.combat.sides,
                    difficulty = nodeJson.phases.combat.difficulty,
                    modifierVar = nodeJson.phases.combat.modifierVar,
                    damageOnSuccess = nodeJson.phases.combat.damageOnSuccess,
                    damageOnCritSuccess = nodeJson.phases.combat.damageOnCritSuccess,
                    damageToPlayerOnFail = nodeJson.phases.combat.damageToPlayerOnFail
                  )
                ),
                transitions = SceneNode.BattleTransitions(
                  winScene = nodeJson.transitions.winScene,
                  loseScene = nodeJson.transitions.loseScene,
                  escapeScene = nodeJson.transitions.escapeScene
                ),
                escape = nodeJson.escape?.let {
                  SceneNode.EscapeConfig(
                    allowed = it.allowed,
                    name = it.name,
                    sides = it.sides,
                    difficulty = it.difficulty,
                    modifierVar = it.modifierVar,
                    onFailPlayerDamage = it.onFailPlayerDamage
                  )
                }
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
data class JumpScenarioJson(
  val scenarioFile: String
) : SceneNodeJson()
@Serializable
sealed class SceneNodeJson {

  @Serializable
  @SerialName("text")
  data class Text(
    val speaker: String? = null,
    val speakerVar: String? = null,
    val text: String
  ) : SceneNodeJson()

  @Serializable
  @SerialName("choice")
  data class Choice(
    val options: List<OptionJson>
  ) : SceneNodeJson()

  @Serializable
  @SerialName("jump")
  data class JumpJson(
    val nextSceneId: String
  ) : SceneNodeJson()

  @Serializable
  @SerialName("setVar")
  data class SetVar(val varName: String, val value: GameValueJson) : SceneNodeJson()

  @Serializable
  @SerialName("battle")
  data class BattleNode(
    val id: String,
    val title: String,
    val monster: MonsterJson,
    val player: PlayerRefsJson,
    val phases: BattlePhasesJson,
    val transitions: BattleTransitionsJson,
    val escape: EscapeJson? = null
  ) : SceneNodeJson()

  @Serializable data class MonsterJson(
    val name: String,
    val image: String,
    val health: Int,
    val horrorDamage: Int = 0,
    val combatDamage: Int = 0
  )

  @Serializable data class PlayerRefsJson(
    val healthVar: String,
    val sanityVar: String
  )

  @Serializable data class BattlePhasesJson(
    val horror: CheckPhaseJson? = null,
    val combat: CombatPhaseJson
  )

  @Serializable data class CheckPhaseJson(
    val enabled: Boolean = true,
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val onFailSanityDamage: Int = 0
  )

  @Serializable data class CombatPhaseJson(
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val damageOnSuccess: Int = 1,
    val damageOnCritSuccess: Int = 2,
    val damageToPlayerOnFail: Int = 1
  )

  @Serializable data class EscapeJson(
    val allowed: Boolean = true,
    val name: String,
    val sides: Int,
    val difficulty: Int,
    val modifierVar: String,
    val onFailPlayerDamage: Int = 0
  )

  @Serializable data class BattleTransitionsJson(
    val winScene: String,
    val loseScene: String,
    val escapeScene: String? = null
  )

  @Serializable
  @SerialName("drawCard")
  data class DrawCard(
    val random: Boolean? = null,
    val value: Int? = null,
    val image: String? = null
  ) : SceneNodeJson()

  @Serializable
  @SerialName("modifyVar")
  data class ModifyVar(val varName: String, val value: GameValueJson,val text: String) : SceneNodeJson()
  @Serializable
  @SerialName("if")
  data class If(
    val variable: String,
    val equals: GameValueJson,
    val successScene: String,
    val failScene: String
  ) : SceneNodeJson()

  @Serializable
  @SerialName("switch")
  data class Switch(
    val variable: String,
    val cases: Map<String, String>,
    val default: String
  ) : SceneNodeJson()

  @Serializable
  @SerialName("switchRange")
  data class SwitchRange(
    val variable: String,
    val ranges: List<RangeCaseJson>,
    val default: String
  ) : SceneNodeJson()

  @Serializable
  data class RangeCaseJson(
    val min: Float,
    val max: Float,
    val scene: String
  )

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

  @Serializable
  @SerialName("randomInt")
  data class RandomInt(val min: Int, val max: Int) : GameValueJson()

  @Serializable
  @SerialName("randomFloat")
  data class RandomFloat(
    val min: Float,
    val max: Float
  ) : GameValueJson()
  fun toGameValue(): GameValue = when (this) {
    is IntVal -> GameValue.IntVal(value)
    is FloatVal -> GameValue.FloatVal(value)
    is BoolVal -> GameValue.Bool(value)
    is StringVal -> GameValue.StringVal(value)
    is RandomInt -> GameValue.RandomInt(min, max)
    is RandomFloat -> GameValue.RandomFloat(min, max)
  }
}
@Serializable
data class OptionJson(
  val text: String,
  val nextSceneId: String
)
@Serializable
@SerialName("background")
data class BackgroundNode(val image: String) : SceneNodeJson()

@Serializable
@SerialName("image")
data class ImageNode(val image: String) : SceneNodeJson()
@Serializable
@SerialName("character")
data class CharacterNode(
  val id: String,

  val image: String? = null,

  val flagVar: String? = null,
  val trueImage: String? = null,
  val falseImage: String? = null,

  val position: String = "center",
  val scale: Float = 1f,
) : SceneNodeJson()
@Serializable
@SerialName("hideImage")
data class HideImageNode(
  val id: String? = null
) : SceneNodeJson()

@Serializable
@SerialName("navigation")
data class SceneViewNode(
  val background: String,

  val navigation: NavigationJson? = null,
  val hotspots: List<HotspotJson> = emptyList()
) : SceneNodeJson()
@Serializable
data class NavigationJson(
  val up: String? = null,
  val down: String? = null,
  val left: String? = null,
  val right: String? = null
)

@Serializable
data class HotspotJson(
  val xPercent: Float,
  val yPercent: Float,
  val widthPercent: Float = 10f,
  val heightPercent: Float = 10f,
  val targetScenarioFile: String
)
@Serializable
@SerialName("characterHide")
data class CharacterHideNode(
  val id: String
) : SceneNodeJson()
@Serializable
@SerialName("initGame")
data class InitGameNode(
  val playerNameVar: String,
  val classVar: String? = null,
  val classes: List<GameClassJson> = emptyList(),
  val nextSceneId: String
) : SceneNodeJson()
@Serializable
data class GameClassJson(
  val id: String,
  val name: String,
  val stats: Map<String, Int> = emptyMap()
)
@Serializable
@SerialName("effect")
data class EffectNode(val image: String) : SceneNodeJson()

