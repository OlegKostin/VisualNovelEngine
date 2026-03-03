package com.olegkos.vnengine.GameLoading

interface ScenarioParser {
  fun parse(raw: String): Scenario
}