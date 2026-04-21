package com.olegkos.save

import com.olegkos.save.conventors.SaveJson
import com.olegkos.save.conventors.toGameState
import com.olegkos.save.conventors.toSerializable
import com.olegkos.vnengine.engine.GameState

class SaveManager(
  private val storage: SaveStorage
) {

  fun save(slot: String, state: GameState, scenario: String) {

    println("=== SAVE START ===")
    println("SLOT: $slot")
    println("SCENARIO: $scenario")
    println("POINTER: ${state.pointer}")
    println("VARS: ${state.variables}")

    val serializable = state.toSerializable(scenario)

    val json = SaveJson.encodeToString(
      GameStateSerializable.serializer(),
      serializable
    )

    println("SERIALIZED JSON: $json")

    storage.save(slot, json)
  }

  fun load(slot: String): LoadedSave? {

    println("=== LOAD START === SLOT=$slot")

    val json = storage.load(slot)
      ?: run {
        println("❌ SAVE NOT FOUND")
        return null
      }

    println("RAW JSON: $json")

    val serializable =
      SaveJson.decodeFromString(
        GameStateSerializable.serializer(),
        json
      )

    println("DESERIALIZED POINTER: ${serializable.pointer}")
    println("DESERIALIZED SCENARIO: ${serializable.scenario}")

    return LoadedSave(
      state = serializable.toGameState(),
      scenario = serializable.scenario
    )
  }
  fun listSaves(): List<String> =
    storage.list()

  fun delete(slot: String) =
    storage.delete(slot)
}

data class LoadedSave(
  val state: GameState,
  val scenario: String
)