package com.olegkos.vnengine.engine.asserts

class AssetPathResolver(
  private val root: String
) {

  fun background(name: String): String =
    "$root/backgrounds/$name"

  fun character(name: String): String =
    "$root/characters/$name"

  fun image(path: String): String =
    "$root/$path"
}