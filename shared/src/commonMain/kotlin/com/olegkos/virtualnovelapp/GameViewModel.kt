package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.scene.Option

class GameViewModel(
  private val engine: VnEngine
) : ViewModel() {

  var currentOutput by mutableStateOf(engine.currentOutput())
    private set

  fun next(option: Option? = null) {
    engine.next(option)
    currentOutput = engine.currentOutput()
  }
}