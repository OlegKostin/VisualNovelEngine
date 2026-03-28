package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.UiState.CharacterState
import com.olegkos.virtualnoveltesttwo.composable.VNTextBox
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
  var characters by remember { mutableStateOf<List<CharacterState>>(emptyList()) }

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

      is EngineOutput.ShowCharacter -> {
        val align = when (o.position) {
          "left" -> Alignment.BottomStart
          "right" -> Alignment.BottomEnd
          else -> Alignment.BottomCenter
        }

        characters = characters
          .filterNot { it.id == o.id } +
            CharacterState(o.id, o.image, align)

        viewModel.next()
      }

      is EngineOutput.HideCharacter -> {
        characters = characters.filterNot { it.id == o.id }
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
          contentScale = ContentScale.Crop
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
            .align(Alignment.BottomCenter),
          contentScale = ContentScale.Fit
        )
      }
    }

    characters.forEach { char ->

      val painter = rememberPainter(
        path = char.image,
        resolver = viewModel.assets,
        reader = viewModel.reader
      )

      painter?.let {
        Image(
          painter = it,
          contentDescription = null,
          modifier = Modifier
            .fillMaxHeight()
            .align(char.alignment),
          contentScale = ContentScale.Fit
        )
      }
    }


    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.Center
    ) {

      when (val o = output) {

        is EngineOutput.ShowText -> {
          VNTextBox(
            speaker = o.speaker,
            text = o.text,
            onNext = { viewModel.next() }
          )
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
    val fullPath = resolver.image(path)
    val bytes = reader.readBytes(fullPath)
    painter = BitmapPainter(loadImageBitmap(bytes.inputStream()))
  }

  return painter
}