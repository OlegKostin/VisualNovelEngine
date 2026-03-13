package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.scene.SceneNode
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

@Composable
fun App(
  viewModel: GameViewModel = koinViewModel()
) {

  val output = viewModel.currentOutput
  var background by remember { mutableStateOf<String?>(null) }
  var image by remember { mutableStateOf<String?>(null) }
  LaunchedEffect(output) {
    when (val o = output) {

      is EngineOutput.ShowBackground -> {
        background = o.image
        viewModel.next()
      }

      is EngineOutput.ShowImage -> {
        image = o.image
        viewModel.next()
      }

      else -> {}
    }
  }

  Box(Modifier.fillMaxSize()) {

    background?.let { bg ->
      Image(
        painter = loadPainter(bg, viewModel.assetsRoot),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
      )
    }

    image?.let { img ->
      Image(
        painter = loadPainter(img, viewModel.assetsRoot),
        contentDescription = null,
        modifier = Modifier
          .fillMaxHeight()
          .align(Alignment.BottomCenter)
      )
    }
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

        Button(onClick = { viewModel. next() }) {
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

      is EngineOutput.ShowDice ->{
        DiceScreen(
          name = output.name,
          sides = output.sides,
          result = output.result,
          modifier = output.modifier,
          difficulty = output.difficulty,
          onRoll = { viewModel.rollDice() },
          onContinue = { viewModel.next() }
        )      }

      else -> {Text("Загрузка...")}
    }
    Spacer(Modifier.height(32.dp))

    SaveSlotsMenu(viewModel)
  }
  }

}
fun loadPainter(path: String, assetsRoot: String): BitmapPainter {

  val file = File("$assetsRoot/$path")
  println("File not found11 : ${file.absolutePath}")
  return BitmapPainter(loadImageBitmap(file.inputStream()))
}