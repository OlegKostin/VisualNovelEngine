package com.olegkos.vnengine.GameLoading

import com.olegkos.vnengine.dsl.scenario

class DevScenarioProvider : ScenarioProvider {

  override suspend fun load(): Scenario {
    val scenes = scenario {
      scene("intro") {
        text("Dev сцена")
      }
    }

    return Scenario(
      startSceneId = "intro",
      scenes = scenes
    )
  }
}