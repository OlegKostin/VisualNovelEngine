package com.olegkos.vnengine.engine

import kotlin.random.Random

class Dice(private val state: GameState) {

  fun roll(diceName: String, sides: Int = 6): Int {
    val allowed = listOf(4, 8, 10, 12, 14, 18, 20)
    val value = allowed.random()
    return value
  }
}

