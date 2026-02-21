package com.olegkos.virtualnovelapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.olegkos.vnengine.engine.GameState
import com.olegkos.vnengine.engine.VnEngine
import com.olegkos.vnengine.scene.Scene
import com.olegkos.vnengine.scene.SceneNode
import com.olegkos.vnengine.scene.Option

class GameViewModel(scenes: Map<String, Scene>) : ViewModel() {

  private val engine = VnEngine(scenes, GameState("intro"))

  // Compose автоматически реагирует на изменения currentNode
  var currentNode by mutableStateOf(engine.currentNode())
    private set

  /**
   * Переход к следующему узлу.
   * Для Choice передаём выбранный option, иначе null.
   */
  fun next(option: Option? = null) {
    engine.next(option)
    currentNode = engine.currentNode()
  }
}