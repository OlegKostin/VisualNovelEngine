package com.olegkos.vnengine.game

import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.GameLoading.ScenarioParser
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import kotlinx.serialization.json.Json
import java.io.File

class GameLoader(
  private val assetReader: AssetReader,
  private val parser: ScenarioParser
) {

  private val json = Json { ignoreUnknownKeys = true }

  suspend fun load(configPath: String): LoadedGame {

    val configRaw = assetReader.readText(configPath)
    val config = json.decodeFromString<GameConfig>(configRaw)

    val baseDir = File(configPath).parent

    val scenarioPath = "$baseDir/${config.startScenario}"

    val scenarioRaw = assetReader.readText(scenarioPath)
    val scenario = parser.parse(scenarioRaw)

    return LoadedGame(
      scenario = scenario,
      assetsRoot = config.assetsRoot,
      variables = config.variables,
      assets = AssetPathResolver(config.assetsRoot, assetReader)
    )  }
}