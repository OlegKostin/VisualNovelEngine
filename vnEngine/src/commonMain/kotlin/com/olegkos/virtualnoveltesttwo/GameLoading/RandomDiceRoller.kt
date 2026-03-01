package com.olegkos.virtualnoveltesttwo.GameLoading

class RandomDiceRoller : DiceRoller {
  override fun roll(sides: Int): Int =
    (1..sides).random()
}