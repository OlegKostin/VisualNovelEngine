package com.olegkos.vnengine.engine

import kotlin.random.Random

class Dice() {

  fun roll(sides: Int): Int {
    return Random.nextInt(1, sides + 1)
  }
}

