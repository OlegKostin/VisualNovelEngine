package com.olegkos.vnengine.GameLoading

interface ScenarioProvider {
  suspend fun load(): Scenario
}