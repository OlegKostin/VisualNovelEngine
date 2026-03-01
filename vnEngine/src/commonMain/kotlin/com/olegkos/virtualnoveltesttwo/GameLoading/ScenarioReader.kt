package com.olegkos.virtualnoveltesttwo.GameLoading

interface ScenarioReader {
  suspend fun read(path: String): String
}