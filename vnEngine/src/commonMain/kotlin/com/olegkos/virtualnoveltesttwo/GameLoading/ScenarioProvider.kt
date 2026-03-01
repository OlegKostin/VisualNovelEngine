package com.olegkos.virtualnoveltesttwo.GameLoading

interface ScenarioProvider {
  suspend fun load(): Scenario
}