package com.olegkos.virtualnoveltesttwo

import com.olegkos.vnengine.dsl.scenario
import com.olegkos.vnengine.scene.Scene

class GameLoader {

  fun load(): Map<String, Scene> {
    return scenario {

      scene("intro") {
        text("Ты просыпаешься в темной камере.")
        text("Перед тобой запертая дверь.")

        choice(
          "Попробовать выбить дверь" to "strength_check",
          "Попробовать вскрыть замок" to "dex_check"
        )
      }

      scene("strength_check") {
        text("Ты разбегаешься и бьёшь плечом в дверь.")

        dice(
          name = "Сила",
          difficulty = 14,
          modifier = 2,
          success = "door_broken",
          fail = "hurt",
          critSuccess = "door_destroyed",
          critFail = "fall_down"
        )
      }

      scene("door_destroyed") {
        text("КРИТИЧЕСКИЙ УСПЕХ!")
        text("Дверь разлетается в щепки.")
        jump("hall")
      }

      scene("door_broken") {
        text("Дверь поддаётся и открывается.")
        jump("hall")
      }

      scene("hurt") {
        text("Дверь выдержала удар.")
        text("Ты ушиб плечо.")
        jump("intro")
      }

      scene("fall_down") {
        text("КРИТИЧЕСКАЯ НЕУДАЧА!")
        text("Ты падаешь на пол.")
        jump("sleep")
      }

      // =====================
      // ЛОВКОСТЬ
      // =====================

      scene("dex_check") {
        text("Ты пытаешься вскрыть замок.")

        dice(
          name = "Ловкость",
          difficulty = 12,
          modifier = 4,
          success = "lock_open",
          fail = "lock_fail",
          critSuccess = "silent_open",
          critFail = "lock_broken"
        )
      }

      scene("silent_open") {
        text("Идеально.")
        text("Замок открывается бесшумно.")
        jump("hall")
      }

      scene("lock_open") {
        text("Замок щёлкает.")
        jump("hall")
      }

      scene("lock_fail") {
        text("Не получается вскрыть.")
        jump("intro")
      }

      scene("lock_broken") {
        text("Отмычка ломается!")
        jump("sleep")
      }

      // =====================
      // ОБЩАЯ СЦЕНА
      // =====================

      scene("hall") {
        text("Ты выходишь в коридор.")

        choice(
          "Идти вперёд" to "end",
          "Вернуться назад" to "intro"
        )
      }

      scene("sleep") {
        text("Ты теряешь сознание...")
        jump("end")
      }

      scene("end") {
        text("Конец демо.")
      }
    }
  }
}