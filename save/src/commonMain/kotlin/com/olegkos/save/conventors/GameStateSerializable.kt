package com.olegkos.save.conventors

import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.GameValueSerializable
import com.olegkos.vnengine.engine.NodePointer
import com.olegkos.vnengine.engine.variables.GameValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class GameStateSerializable(
  val pointer: NodePointer,
  val variables: Map<String, GameValueSerializable>,
  val diceResult: Int? = null,
  val timestamp: Long,
  val scenario: String
)
fun GameValue.toSerializable(): GameValueSerializable = when(this) {
  is GameValue.IntVal -> GameValueSerializable.IntVal(value)
  is GameValue.FloatVal -> GameValueSerializable.FloatVal(value)
  is GameValue.Bool -> GameValueSerializable.BoolVal(value)
  is GameValue.StringVal -> GameValueSerializable.StringVal(value)
  else -> throw IllegalArgumentException("Random values should be resolved before saving")
}

fun GameValueSerializable.toGameValue(): GameValue = when(this) {
  is GameValueSerializable.IntVal -> GameValue.IntVal(value)
  is GameValueSerializable.FloatVal -> GameValue.FloatVal(value)
  is GameValueSerializable.BoolVal -> GameValue.Bool(value)
  is GameValueSerializable.StringVal -> GameValue.StringVal(value)
}
fun GameState.toSerializable(): GameStateSerializable =
  GameStateSerializable(
    pointer = pointer,
    variables = variables.mapValues { it.value.toSerializable() },
    diceResult = diceResult,
    timestamp = TODO(),
    scenario = TODO(),
  )
fun GameStateSerializable.toGameState(): GameState =
  GameState(
    pointer = pointer,
    variables = variables
      .mapValues { it.value.toGameValue() }
      .toMutableMap(),
    diceResult = diceResult
  )
val SaveJson = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
}
fun GameStateSerializable.toJson(): String =
  SaveJson.encodeToString(GameStateSerializable.serializer(), this)

fun gameStateFromJson(json: String): GameStateSerializable =
  SaveJson.decodeFromString(GameStateSerializable.serializer(), json)