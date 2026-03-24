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
    println("=== LOAD GAME CONFIG ===")
    println("CONFIG PATH: $configPath")
    val configRaw = assetReader.readText(configPath)
    println("CONFIG RAW START >>>")
    println(configRaw)
    println("CONFIG RAW END <<<")
    println("CONFIG RAW LENGTH: ${configRaw.length}")
    val config = json.decodeFromString<GameConfig>(configRaw)
    println("PARSED CONFIG: $config")

    val baseDir = File(configPath).parent

    val scenarioPath = "$baseDir/${config.startScenario}"
    println("SCENARIO PATH: $scenarioPath")
    val scenarioRaw = assetReader.readText(scenarioPath)

    println("SCENARIO RAW START >>>")
    println(scenarioRaw.take(200))
    println("SCENARIO RAW END <<<")
    val scenario = parser.parse(scenarioRaw)

    return LoadedGame(
      scenario = scenario,
      assetsRoot = config.assetsRoot,
      variables = config.variables,
      assets = AssetPathResolver(config.assetsRoot, assetReader)
    )  }
}