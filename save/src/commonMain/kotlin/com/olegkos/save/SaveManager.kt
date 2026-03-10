package com.olegkos.save

import com.olegkos.save.conventors.GameStateSerializable
import com.olegkos.save.conventors.SaveJson
import com.olegkos.save.conventors.gameStateFromJson
import com.olegkos.save.conventors.toGameState
import com.olegkos.save.conventors.toJson
import com.olegkos.save.conventors.toSerializable
import com.olegkos.vnengine.engine.GameState

class SaveManager(
  private val storage: SaveStorage
) {

  fun save(slot: String, state: GameState, scenario: String) {

    val serializable = GameStateSerializable(
      pointer = state.pointer,
      variables = state.variables.mapValues { it.value.toSerializable() },
      diceResult = state.diceResult,
      timestamp = System.currentTimeMillis(),
      scenario = scenario
    )

    val json = SaveJson.encodeToString(serializable)

    storage.save(slot, json)
  }
  fun load(slot: String): GameState? {

    val json = storage.load(slot)
      ?: return null

    return gameStateFromJson(json)
      .toGameState()
  }

  fun listSaves(): List<String> =
    storage.list()

  fun delete(slot: String) =
    storage.delete(slot)
}