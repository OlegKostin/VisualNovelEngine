package com.olegkos.vnengine.engine.academy

import kotlinx.serialization.json.Json

object AcademyConfigLoader {
  private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
  }

  fun parse(raw: String): AcademyConfig = json.decodeFromString(raw)
}
