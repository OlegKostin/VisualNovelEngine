package com.olegkos.virtualnoveltesttwo.GameLoading

class FileScenarioProvider(
  private val reader: ScenarioReader,
  private val parser: ScenarioParser,
  private val path: String
) : ScenarioProvider {

  override suspend fun load(): Scenario {
    val raw = reader.read(path)
    return parser.parse(raw)
  }
}