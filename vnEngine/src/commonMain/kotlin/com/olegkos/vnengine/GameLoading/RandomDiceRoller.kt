package com.olegkos.vnengine.GameLoading

class RandomDiceRoller : DiceRoller {
  override fun roll(sides: Int): Int =
    (1..sides).random()
}