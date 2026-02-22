package com.olegkos.virtualnovelapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.dsl.scenario
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.scene.SceneNode

@Composable
fun App(viewModel: GameViewModel? = null) {
  val scenes = remember {
    scenario {
      scene("intro") {
        text("Ты просыпаешься в темноте.")
        choice("Встать" to "hall", "Лежать" to "sleep")
      }
      scene("hall") {
        text("Ты в коридоре. Нужно бросить кубик, чтобы решить дальнейший путь.")
        dice("Кубик судьбы", 20)
        choice("Продолжить" to "end")
      }
      scene("sleep") {
        text("Ты снова засыпаешь...")
        jump("end")
      }
      scene("end") {
        text("Конец истории.")
      }
    }
  }

  val vm = viewModel ?: remember { GameViewModel(scenes) }
  val output = vm.currentOutput // напрямую наблюдаем mutableStateOf

  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.Center
  ) {
    when (val o = output) {
      is EngineOutput.ShowText -> {
        Text(o.text)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.next() }) {
          Text("Далее")
        }
      }
      is EngineOutput.ShowChoices -> {
        o.options.forEach { option ->
          Button(
            onClick = { vm.next(option) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
          ) {
            Text(option.text)
          }
        }
      }
    }
  }
}