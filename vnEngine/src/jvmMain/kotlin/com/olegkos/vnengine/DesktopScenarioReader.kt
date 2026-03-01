package com.olegkos.vnengine

import com.olegkos.virtualnoveltesttwo.GameLoading.ScenarioReader

class DesktopScenarioReader : ScenarioReader {

  override suspend fun read(path: String): String {
    return java.io.File(path).readText()
  }
}