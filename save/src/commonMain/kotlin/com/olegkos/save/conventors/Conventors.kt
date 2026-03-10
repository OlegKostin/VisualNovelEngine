package com.olegkos.save.conventors

import com.olegkos.save.GameStateSerializable
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.GameValueSerializable
import com.olegkos.vnengine.engine.variables.GameValue
import kotlinx.serialization.json.Json



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
fun GameState.toSerializable(
  scenario: String,
  timestamp: Long = System.currentTimeMillis()
): GameStateSerializable =
  GameStateSerializable(
    pointer = pointer,
    variables = variables.mapValues { it.value.toSerializable() },
    diceResult = diceResult,
    timestamp = timestamp,
    scenario = scenario
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