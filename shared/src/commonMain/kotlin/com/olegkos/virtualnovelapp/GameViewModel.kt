  package com.olegkos.virtualnovelapp

  import androidx.compose.runtime.getValue
  import androidx.compose.runtime.mutableStateOf
  import androidx.compose.runtime.setValue
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.olegkos.save.SaveManager
  import com.olegkos.vnengine.GameLoading.AssetReader
  import com.olegkos.vnengine.GameLoading.DiceRoller
  import com.olegkos.vnengine.GameLoading.ScenarioParser
  import com.olegkos.vnengine.engine.EngineOutput
  import com.olegkos.vnengine.engine.asserts.AssetPathResolver
  import com.olegkos.vnengine.game.GameLoader
  import com.olegkos.vnengine.scene.Option
  import com.olegkos.vnengine.scene.SceneNode
  import kotlinx.coroutines.launch

  class GameViewModel(
    private val controller: GameController,
    private val saveManager: SaveManager
  ) : ViewModel() {

    val assets: AssetPathResolver
      get() = controller.assets

    val reader: AssetReader
      get() = controller.reader

    var currentOutput by mutableStateOf<EngineOutput>(EngineOutput.Loading)
      private set

    var currentNode by mutableStateOf<SceneNode?>(null)
      private set

    init {
      viewModelScope.launch {
        val (output, node) = controller.init()
        currentOutput = output
        currentNode = node
      }
    }

    fun next(option: Option? = null) {
      val result = controller.next(option)
      currentOutput = result.first
      currentNode = result.second

      if (result.first is EngineOutput.JumpScenarioOutput) {
        viewModelScope.launch {
          val jump = result.first as EngineOutput.JumpScenarioOutput
          val (output, node) = controller.switchScenario(jump.scenarioFile)
          currentOutput = output
          currentNode = node
        }
      }
    }

    fun rollDice() {
      val (output, node) = controller.rollDice()
      currentOutput = output
      currentNode = node
    }

    fun saveGame(slot: String) {
      controller.saveGame(slot)
    }

    fun loadSave(slot: String) {
      viewModelScope.launch {
        val (output, node) = controller.loadSave(slot)
        currentOutput = output
        currentNode = node
      }
    }

    fun listSaves(): List<String> =
      saveManager.listSaves()
  }