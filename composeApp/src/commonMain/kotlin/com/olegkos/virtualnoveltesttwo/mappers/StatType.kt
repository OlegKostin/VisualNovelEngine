package com.olegkos.virtualnoveltesttwo.mappers

import org.jetbrains.compose.resources.DrawableResource
import virtualnoveltesttwo.composeapp.generated.resources.Res
import virtualnoveltesttwo.composeapp.generated.resources.stat_health
import virtualnoveltesttwo.composeapp.generated.resources.stat_luck
import virtualnoveltesttwo.composeapp.generated.resources.stat_mental

enum class StatType(
  val key: String,
  val image: DrawableResource
) {
//  STR("opt_str", Res.drawable.stat_str),
//  WIS("opt_wisdom", Res.drawable.stat_wisdom),
//  WILL("opt_will", Res.drawable.stat_will),
  LUCK("opt_luck", Res.drawable.stat_luck),
  HP("health", Res.drawable.stat_health),
  MENTAL("mental_health", Res.drawable.stat_mental);

  companion object {
    fun fromKey(key: String): StatType? {
      return entries.find { it.key == key }
    }
  }
}