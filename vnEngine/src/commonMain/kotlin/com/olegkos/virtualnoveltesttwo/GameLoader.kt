package com.olegkos.virtualnoveltesttwo

import com.olegkos.vnengine.dsl.scenario
import com.olegkos.vnengine.scene.Scene

class GameLoader {
  fun load(): Map<String, Scene> {
    return scenario {
      scene("intro") {
        text("Ты просыпаешься11 в темноте.")
        choice("Встать" to "hall", "Лежать" to "sleep")
      }
      scene("hall") {
        text("Ты в коридоре.")
        jump("end")
      }
      scene("sleep") {
        text("Ты снова засыпаешь...")
        jump("end")
      }
      scene("end") {
        text("Конец истории.")
      }
    }
  }
}