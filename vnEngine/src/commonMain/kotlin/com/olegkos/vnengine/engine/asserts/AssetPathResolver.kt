package com.olegkos.vnengine.engine.asserts

import com.olegkos.vnengine.GameLoading.AssetReader
import java.io.File

class AssetPathResolver(
  private val root: String,
  private val reader: AssetReader
) {

  fun background(name: String) =
    "backgrounds/$name"

  fun character(name: String) =
    "characters/$name"

  fun image(path: String): String =
    "$root/$path"

  suspend fun readBytes(path: String): ByteArray {
    return reader.readBytes("$root/$path")
  }

  suspend fun readText(path: String): String {
    return reader.readText("$root/$path")
  }
}