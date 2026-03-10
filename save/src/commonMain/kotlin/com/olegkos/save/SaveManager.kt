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

  fun load(slot: String): GameState? {

    val json = storage.load(slot)
      ?: return null

    val serializable =
      SaveJson.decodeFromString(
        GameStateSerializable.serializer(),
        json
      )

    return serializable.toGameState()
  }

  fun listSaves(): List<String> =
    storage.list()

  fun delete(slot: String) =
    storage.delete(slot)
}