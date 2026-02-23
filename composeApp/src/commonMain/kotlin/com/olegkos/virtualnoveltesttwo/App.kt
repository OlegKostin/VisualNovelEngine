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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
  viewModel: GameViewModel = koinViewModel()
) {

  val output = viewModel.currentOutput

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center
  ) {
    when (val o = output) {

      is EngineOutput.ShowText -> {
        Text(o.text)

        Spacer(Modifier.height(16.dp))

        Button(onClick = { viewModel.next() }) {
          Text("Далее")
        }
      }

      is EngineOutput.ShowChoices -> {
        o.options.forEach { option ->
          Button(
            onClick = { viewModel.next(option) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Text(option.text)
          }
        }
      }
    }
  }
}