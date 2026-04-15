package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.UiState.CharacterState
import com.olegkos.virtualnoveltesttwo.composable.InitGameScreen
import com.olegkos.virtualnoveltesttwo.composable.ShowVarScreen
import com.olegkos.virtualnoveltesttwo.composable.VNTextBox
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.scene.Option
import com.olegkos.vnengine.scene.SubClass
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: GameViewModel = koinViewModel()) {
  val output = viewModel.currentOutput

  var background by remember { mutableStateOf<String?>(null) }
  var image by remember { mutableStateOf<String?>(null) }
  var characters by remember { mutableStateOf<List<CharacterState>>(emptyList()) }
  var sceneView by remember { mutableStateOf<EngineOutput.ShowSceneView?>(null) }


  fun positionOffsetFromString(position: String, boxWidth: Dp): Dp {
    val index = position.lowercase().removePrefix("pos").toIntOrNull() ?: 0
    val step = boxWidth * 0.10f // 10% ширины
    return step * index
  }

  LaunchedEffect(output) {
    if (output !is EngineOutput.ShowSceneView) {
      sceneView = null
    }
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
        val scale = o.scale
        val position = o.position

        characters = characters
          .filterNot { it.id == o.id } +
            CharacterState(
              id = o.id,
              image = o.image,
              alignment = Alignment.BottomStart,
              scale = scale,
              position = position
            )

        viewModel.next()
      }
      is EngineOutput.HideImage -> {
        image = null
        viewModel.next()
      }

      is EngineOutput.HideCharacter -> {
        characters = characters.filterNot { it.id == o.id }
        viewModel.next()
      }

      is EngineOutput.ShowSceneView -> {
        sceneView = o
        background = o.background
      }

      else -> Unit
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val boxWidth = maxWidth

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
          contentScale = ContentScale.FillHeight
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
        val xOffset = positionOffsetFromString(char.position, boxWidth)
        Image(
          painter = it,
          contentDescription = null,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = xOffset)
            .graphicsLayer(
              scaleX = char.scale,
              scaleY = char.scale,
              transformOrigin = TransformOrigin(0.5f, 1f)
            ),
          contentScale = ContentScale.Fit
        )
      }
    }

    sceneView?.let { view ->


      background = view.background

      val boxHeight = maxHeight


      view.navigation?.let { nav ->
        nav.left?.let { target ->
          ArrowButton(Alignment.CenterStart) {
            viewModel.jumpScenario(target)
          }
        }
        nav.right?.let { target ->
          ArrowButton(Alignment.CenterEnd) {
            viewModel.jumpScenario(target)
          }
        }
        nav.up?.let { target ->
          ArrowButton(Alignment.TopCenter) {
            viewModel.jumpScenario(target)
          }
        }
        nav.down?.let { target ->
          ArrowButton(Alignment.BottomCenter) {
            viewModel.jumpScenario(target)
          }
        }
      }

      view.hotspots.forEach { spot ->
        Box(
          modifier = Modifier
            .offset {
              IntOffset(
                (boxWidth.value * spot.xPercent / 100f).toInt(),
                (boxHeight.value * spot.yPercent / 100f).toInt()
              )
            }
            .size(
              boxWidth * (spot.widthPercent / 100f),
              boxHeight * (spot.heightPercent / 100f)
            )
            .clickable {
              viewModel.jumpScenario(spot.targetScenarioFile)
            }
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
        is EngineOutput.ShowInitGame -> {
          InitGameScreen(
            classes = o.classes,
            onConfirm = { name: String, selectedClass: SubClass.GameClass? ->
              viewModel.initGame(
                playerName = name,
                selectedClass = selectedClass,
                playerNameVar = o.playerNameVar,
                classVar = o.classVar
              )
            }
          )
        }
        is EngineOutput.ShowVar -> {
          ShowVarScreen(
            name = o.name,
            value = o.value,
            description = o.text,
            onNext = { viewModel.next() }
          )
      }
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
            phase = o.phase,
            onContinue = { viewModel.next() },
            onApplyCard = { value -> viewModel.applyDiceModifier(value) }
          )
        }
        is EngineOutput.ShowSceneView -> Unit
        else -> Text("Загрузка...")
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

@Composable
fun ArrowButton(
  alignment: Alignment,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = alignment
  ) {
    Button(
      onClick = onClick,
      modifier = Modifier.size(60.dp)
    ) {
      Text(">")
    }
  }
}