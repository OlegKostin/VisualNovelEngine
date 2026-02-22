package com.olegkos.coredi

import com.olegkos.vnengine.dsl.scenario
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine


fun provideVnEngine(): VnEngine {
  val scenes = scenario {
    scene("intro") {
      text("Ты просыпаешься в темноте.")
      choice("Встать" to "hall", "Лежать" to "sleep")
    }
    scene("hall") {
      text("Ты в коридоре. Нужно бросить кубик, чтобы решить дальнейший путь.")
      dice("Кубик судьбы", 20)
      choice("Продолжить" to "end")
    }
    scene("sleep") {
      text("Ты снова засыпаешь...")
      jump("end")
    }
    scene("end") {
      text("Конец истории.")
    }
  }
  return VnEngine(scenes, GameState("intro"))
}