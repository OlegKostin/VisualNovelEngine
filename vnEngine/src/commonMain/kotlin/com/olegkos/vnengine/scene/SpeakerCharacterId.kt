package com.olegkos.vnengine.scene

/**
 * Связь [speakerVar] из ноды text с id персонажа (нода character).
 * `teacher_name` → `teacher`, `player_name` → `main`.
 */
fun speakerVarToCharacterId(speakerVar: String?): String? {
  val key = speakerVar?.trim()?.takeIf { it.isNotEmpty() } ?: return null
  return when (key) {
    "player_name" -> "main"
    else -> key.removeSuffix("_name").removeSuffix("_var").takeIf { it.isNotEmpty() }
  }
}
