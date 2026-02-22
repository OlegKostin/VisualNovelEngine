package com.olegkos.vnengine.engine

object DiceFrames {
  private val framesMap: Map<String, List<String>> = mapOf(

  )

  fun getFrames(name: String): List<String> {
    return framesMap[name] ?: error("No frames for dice '$name'")
  }
}