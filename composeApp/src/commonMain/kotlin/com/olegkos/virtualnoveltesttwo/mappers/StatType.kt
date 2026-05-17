package com.olegkos.virtualnoveltesttwo.mappers

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource
import virtualnoveltesttwo.composeapp.generated.resources.Res
import virtualnoveltesttwo.composeapp.generated.resources.dark_flow
import virtualnoveltesttwo.composeapp.generated.resources.light_flow
import virtualnoveltesttwo.composeapp.generated.resources.stat_health
import virtualnoveltesttwo.composeapp.generated.resources.stat_luck
import virtualnoveltesttwo.composeapp.generated.resources.stat_mental
import virtualnoveltesttwo.composeapp.generated.resources.stat_str
import virtualnoveltesttwo.composeapp.generated.resources.stat_will
import virtualnoveltesttwo.composeapp.generated.resources.stat_wisdom

@Immutable
enum class StatType(
  val key: String,
  val image: DrawableResource,
  val title: String,
  /** Короткая подсказка при наведении на иконку */
  val hoverHint: String
) {
  STR("opt_str", Res.drawable.stat_str, "сила", "Физическая мощь и проверки ближнего боя."),
  WIS("opt_wisdom", Res.drawable.stat_wisdom, "Мудрость", "Знания, внимательность и магические проверки."),
  WILL("opt_will", Res.drawable.stat_will, "Воля", "Сопротивление стрессу и проверкам ужаса."),
  LUCK("opt_luck", Res.drawable.stat_luck, "Удача", "Добавляется к проверкам силы, мудрости, воли, тьмы и света."),
  HP("health", Res.drawable.stat_health, "Здоровье", "Запас выносливости до поражения."),
  MENTAL("mental_health", Res.drawable.stat_mental, "Рассудок", "Рассудок и устойчивость к ужасу."),
  DARK("opt_dark", Res.drawable.dark_flow, "Течение Тьмы", "Рассудок и устойчивость к ужасу."),
  LIGHT("opt_light", Res.drawable.light_flow, "Течение Света", "Рассудок и устойчивость к ужасу.");

  companion object {
    fun fromKey(key: String): StatType? {
      return entries.find { it.key == key }
    }

    fun hoverHintForKey(key: String): String {
      return fromKey(key)?.hoverHint ?: when (key) {
        "class" -> "Название или метка класса."
        "class_number" -> "Порядковый номер варианта класса."
        else -> "Параметр «$key»."
      }
    }
  }
}