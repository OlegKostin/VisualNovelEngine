package com.olegkos.vnengine.engine.asserts

import com.olegkos.vnengine.GameLoading.AssetReader
import java.io.File

class AssetPathResolver(
  private val root: String
) : AssetReader {

  fun background(name: String): String =
    "$root/backgrounds/$name"

  fun character(name: String): String =
    "$root/characters/$name"

  fun image(path: String): String =
    "$root/$path"

  override suspend fun readText(path: String): String {
    val file = File(root, path)
    return file.readText()
  }

  override suspend fun readBytes(path: String): ByteArray {
    val file = File(root, path)
    return file.readBytes()
  }
}