package com.olegkos.vnengine.game

import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.ScenarioParser
import kotlinx.serialization.json.Json

class GameLoader(
  private val assets: AssetReader,
  private val parser: ScenarioParser
) {

  private val json = Json {
    ignoreUnknownKeys = true
  }

  suspend fun load(configPath: String): LoadedGame {


    val configRaw = assets.readText(configPath)

    val config =
      json.decodeFromString<GameConfig>(configRaw)

    // читаем сценарий
    val scenarioRaw =
      assets.readText(config.startScenario)

    val scenario =
      parser.parse(scenarioRaw)

    return LoadedGame(
      scenario = scenario,
      assetsRoot = config.assetsRoot
    )
  }
}