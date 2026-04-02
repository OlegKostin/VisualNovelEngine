package com.olegkos.virtualnoveltesttwo.mappers

import org.jetbrains.compose.resources.DrawableResource
import virtualnoveltesttwo.composeapp.generated.resources.Res
import virtualnoveltesttwo.composeapp.generated.resources.stat_health
import virtualnoveltesttwo.composeapp.generated.resources.stat_luck
import virtualnoveltesttwo.composeapp.generated.resources.stat_mental

enum class StatType(
  val key: String,
  val image: DrawableResource,
  val title: String
) {
  LUCK("opt_luck", Res.drawable.stat_luck, "Удача"),
  HP("health", Res.drawable.stat_health, "Здоровье"),
  MENTAL("mental_health", Res.drawable.stat_mental, "Психика");

  companion object {
    fun fromKey(key: String): StatType? {
      return entries.find { it.key == key }
    }
  }
}