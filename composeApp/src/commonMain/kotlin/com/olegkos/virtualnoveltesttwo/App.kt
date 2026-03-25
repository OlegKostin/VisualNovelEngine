package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import org.koin.compose.viewmodel.koinViewModel

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

      else -> Unit
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {


    background?.let { bgPath ->
      val painter = rememberPainter(
        path = bgPath,
        resolver = viewModel.assets,
        reader = viewModel.reader
      )

      painter?.let {
        Image(
          painter = it,
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop // 🔥 важно!
        )
      }
    }


    image?.let { imgPath ->
      val painter = rememberPainter(
        path = imgPath,
        resolver = viewModel.assets,
        reader = viewModel.reader
      )

      painter?.let {
        Image(
          painter = it,
          contentDescription = null,
          modifier = Modifier
            .fillMaxHeight()
            .align(Alignment.BottomCenter)
        )
      }
    }

    // ✅ UI слой
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

        is EngineOutput.ShowDice -> {
          DiceScreen(
            name = o.name,
            sides = o.sides,
            result = o.result,
            modifier = o.modifier,
            difficulty = o.difficulty,
            onRoll = { viewModel.rollDice() },
            onContinue = { viewModel.next() }
          )
        }

        else -> {
          Text("Загрузка...")
        }
      }

      Spacer(Modifier.height(32.dp))

      SaveSlotsMenu(viewModel)
    }
  }
}

@Composable
fun rememberPainter(
  path: String,
  resolver: AssetPathResolver,
  reader: AssetReader
): BitmapPainter? {

  var painter by remember { mutableStateOf<BitmapPainter?>(null) }

  LaunchedEffect(path) {
    val fullPath = resolver.image(path) // строим путь
    val bytes = reader.readBytes(fullPath) // читаем
    painter = BitmapPainter(loadImageBitmap(bytes.inputStream()))
  }

  return painter
}