package com.olegkos.vnengine.engine.variables

/**
 * Переменные, для которых [com.olegkos.vnengine.scene.SceneNode.ModifyVar] с непустым `text`
 * показывает экран ShowVar (иконка + текст). Совпадает с StatType в composeApp.
 */
val MODIFY_VAR_DISPLAY_VAR_NAMES: Set<String> = setOf(
  "opt_str",
  "opt_wisdom",
  "opt_will",
  "opt_luck",
  "health",
  "mental_health",
  "opt_dark",
  "opt_light",
)

fun shouldShowModifyVarUi(varName: String, text: String?): Boolean =
  !text.isNullOrBlank() && varName in MODIFY_VAR_DISPLAY_VAR_NAMES

fun GameValue.formatModifyVarDelta(): String {
  val r = resolve()
  return when (r) {
    is GameValue.IntVal ->
      if (r.value > 0) "+${r.value}" else r.value.toString()
    is GameValue.FloatVal -> {
      val v = r.value
      if (v > 0f) "+$v" else v.toString()
    }
    else -> r.forStatPreview()
  }
}
