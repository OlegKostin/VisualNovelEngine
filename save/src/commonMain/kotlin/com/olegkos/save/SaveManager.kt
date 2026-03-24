package com.olegkos.save

import com.olegkos.save.conventors.SaveJson
import com.olegkos.save.conventors.toGameState
import com.olegkos.save.conventors.toSerializable
import com.olegkos.vnengine.engine.GameState

class SaveManager(
  private val storage: SaveStorage
) {

  fun save(slot: String, state: GameState, scenario: String) {

    val serializable = state.toSerializable(scenario)

    val json = SaveJson.encodeToString(
      GameStateSerializable.serializer(),
      serializable
    )

    storage.save(slot, json)
  }

  fun load(slot: String): LoadedSave? {

    val json = storage.load(slot)
      ?: return null

    val serializable =
      SaveJson.decodeFromString(
        GameStateSerializable.serializer(),
        json
      )

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