package com.olegkos.vnengine.engine.asserts

import com.olegkos.vnengine.GameLoading.AssetReader
import java.io.File

class AssetPathResolver(
  private val root: String,
  private val reader: AssetReader
) {

  fun background(path: String) =
    "$root/backgrounds/$path"

  fun character(path: String) =
    "$root/$path"

  fun image(path: String) =
    "$root/$path" // общий fallback (если нужен)

  fun card(path: String) =
    "game/$path"

  suspend fun readBytes(path: String): ByteArray {
    return reader.readBytes("$root/$path")
  }

  suspend fun readText(path: String): String {
    return reader.readText("$root/$path")
  }
}