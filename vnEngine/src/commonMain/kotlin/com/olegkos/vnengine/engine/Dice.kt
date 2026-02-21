package com.olegkos.vnengine.engine

import kotlin.random.Random

class Dice(private val state: GameState) {

  fun roll(diceName: String, sides: Int = 6): Int {
    state.consumeDiceResult(diceName)?.let { return it }


    val value = Random.nextInt(1, sides + 1)
    return value
  }
}

