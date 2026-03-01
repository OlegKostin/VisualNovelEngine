package com.olegkos.virtualnoveltesttwo.GameLoading

interface ScenarioParser {
  fun parse(raw: String): Scenario
}