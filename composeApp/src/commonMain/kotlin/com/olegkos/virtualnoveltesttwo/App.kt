package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.composable.GameMenuHub
import com.olegkos.virtualnoveltesttwo.composable.InitGameScreen
import com.olegkos.virtualnoveltesttwo.composable.PlayerStatsScreen
import com.olegkos.virtualnoveltesttwo.composable.ShowVarScreen
import com.olegkos.virtualnoveltesttwo.composable.VNTextBox
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.EngineOutput
import com.olegkos.vnengine.engine.asserts.AssetPathResolver
import com.olegkos.vnengine.scene.SceneNode.NavLink
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay

@Composable
fun App(viewModel: GameViewModel = koinViewModel()) {

  val output = viewModel.currentOutput

  var gameMenu by remember { mutableStateOf(GameMenu.None) }
  var pendingScreenshotSaveSlot by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(pendingScreenshotSaveSlot) {
    val slot = pendingScreenshotSaveSlot ?: return@LaunchedEffect
    gameMenu = GameMenu.None
    delay(150)
    val png = captureScreenshotPngBytes()
    viewModel.saveGame(slot, png)
    pendingScreenshotSaveSlot = null
    gameMenu = GameMenu.SaveLoad
  }

  var background by remember { mutableStateOf<String?>(null) }
  var image by remember { mutableStateOf<String?>(null) }
  var sceneView by remember { mutableStateOf<EngineOutput.ShowSceneView?>(null) }
  var cardImage by remember { mutableStateOf<String?>(null) }

  var advancing by remember { mutableStateOf(false) }

  fun positionOffsetFromString(position: String, boxWidth: Dp): Dp {
    val index = position.lowercase().removePrefix("pos").toIntOrNull() ?: 0
    val step = boxWidth * 0.1f
    return step * index
  }

  LaunchedEffect(sceneView) {
    sceneView?.let {
      background = it.background
    }
  }

  LaunchedEffect(output) {
    if (advancing) return@LaunchedEffect
    advancing = true

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

      is EngineOutput.HideImage -> {
        image = null
        viewModel.next()
      }

      is EngineOutput.ShowCard -> {
        cardImage = o.image
      }

      is EngineOutput.ShowSceneView -> {
        sceneView = o
      }

      else -> Unit
    }

    advancing = false
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

    val boxWidth = maxWidth

    background?.let { bgPath ->
      val painter = rememberPainter(viewModel.assets.background(bgPath), viewModel.reader)
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
      val painter = rememberPainter(viewModel.assets.image(imgPath), viewModel.reader)
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

    cardImage?.let { cardPath ->
      val painter = rememberPainter(viewModel.assets.card(cardPath), viewModel.reader)
      painter?.let {
        Image(
          painter = it,
          contentDescription = null,
          modifier = Modifier
            .height(maxHeight * 0.95f)
            .align(Alignment.Center),
          contentScale = ContentScale.Fit
        )
      }
    }

    if (viewModel.isReady) {
      val reader = viewModel.reader
      val assets = viewModel.assets
      viewModel.visibleCharacters.forEach { char ->
        key(char.id) {
          val painter = rememberPainter(assets.character(char.image), reader)
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
      }
    }

    sceneView?.let { view ->

      val boxHeight = maxHeight

      view.navigation?.let { nav ->
        nav.left?.let { link ->
          SceneNavControl(
            alignment = Alignment.CenterStart,
            link = link,
            assets = viewModel.assets,
            reader = viewModel.reader
          ) { viewModel.jumpScenario(link.scenarioFile) }
        }
        nav.right?.let { link ->
          SceneNavControl(
            alignment = Alignment.CenterEnd,
            link = link,
            assets = viewModel.assets,
            reader = viewModel.reader
          ) { viewModel.jumpScenario(link.scenarioFile) }
        }
        nav.up?.let { link ->
          SceneNavControl(
            alignment = Alignment.TopCenter,
            link = link,
            assets = viewModel.assets,
            reader = viewModel.reader
          ) { viewModel.jumpScenario(link.scenarioFile) }
        }
        nav.down?.let { link ->
          SceneNavControl(
            alignment = Alignment.BottomCenter,
            link = link,
            assets = viewModel.assets,
            reader = viewModel.reader
          ) { viewModel.jumpScenario(link.scenarioFile) }
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
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center
    ) {

      when (val o = output) {

        is EngineOutput.ShowInitGame -> {
          InitGameScreen(
            classes = o.classes,
            cardPainter = { path ->
              rememberPainter(viewModel.assets.card(path), viewModel.reader)
            },
            resolveStartingCardPreview = { classId, slot, spec ->
              viewModel.previewStartingCard(classId, slot, spec)
            },
            onConfirm = { name, selectedClass ->
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
          if (o.text.isNullOrBlank()) {
            viewModel.next()
          } else {
            ShowVarScreen(
              name = o.name,
              value = o.value,
              description = o.text!!,
              onNext = { viewModel.next() }
            )
          }
        }

        is EngineOutput.ShowDiceDuel -> {
          DiceDuelScreen(
            title = o.title,
            sides = o.sides,
            playerName = o.playerName,
            playerModifier = o.playerModifier,
            playerRoll = o.playerRoll,
            playerTotal = o.playerTotal,
            opponentName = o.opponentName,
            opponentImagePainter = rememberPainter(
              viewModel.assets.image(o.opponentImage),
              viewModel.reader
            ),
            opponentModifier = o.opponentModifier,
            opponentRoll = o.opponentRoll,
            opponentTotal = o.opponentTotal,
            phase = o.phase,
            cards = o.cards,
            canUseCards = o.canUseCards,
            resultText = o.resultText,
            cardPainter = { path ->
              rememberPainter(viewModel.assets.card(path), viewModel.reader)
            },
            onRoll = { viewModel.diceDuelRoll() },
            onApplyCards = { value, usedCards ->
              viewModel.diceDuelApplyModifier(value, usedCards)
            },
            onContinue = { viewModel.diceDuelContinue() }
          )
        }

        is EngineOutput.ShowText -> {
          VNTextBox(
            speaker = o.speaker,
            text = o.text,
            onNext = { viewModel.next() }
          )
        }

        is EngineOutput.ShowCard -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clickable {
                cardImage = null
                viewModel.next()
              },
            contentAlignment = Alignment.Center
          ) {}
        }

        is EngineOutput.ShowChoices -> {
          o.options.forEach { option ->
            VnOutlinedButton(
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
            cards = viewModel.getCards(),
            onContinue = { viewModel.next() },
            cardPainter = { path ->
              rememberPainter(viewModel.assets.card(path), viewModel.reader)
            },
            onApplyCard = { value, usedCards ->
              viewModel.applyDiceModifier(value, usedCards)
            }
          )
        }

        is EngineOutput.ShowCardGame -> {
          CardGameScreen(
            output = o,
            viewModel = viewModel,
            cardPainter = { path ->
              rememberPainter(viewModel.assets.card(path), viewModel.reader)
            }
          )
        }

        is EngineOutput.ShowAcademyHub -> {
          if (viewModel.isReady) {
            AcademyHubScreen(
              output = o,
              viewModel = viewModel,
              backgroundPainter = { path ->
                rememberPainter(viewModel.assets.background(path), viewModel.reader)
              },
            )
          }
        }

        is EngineOutput.ShowBattle -> {
          Box(modifier = Modifier.fillMaxSize()) {
            BattleScreen(
              title = o.title,
              playerName = o.playerName ?: "",
              monsterName = o.monsterName,
              monsterImagePainter = rememberPainter(
                viewModel.assets.image(o.monsterImage),
                viewModel.reader
              ),
              monsterHp = o.monsterHp,
              monsterCombatDamage = o.monsterCombatDamage,
              monsterHorrorDamage = o.monsterHorrorDamage,
              playerHealth = o.playerHealth,
              playerSanity = o.playerSanity,
              phase = o.phase,
              diceName = o.diceName,
              sides = o.sides,
              difficulty = o.difficulty,
              result = o.result,
              modifier = o.modifier,
              canUseCards = o.canUseCards,
              canEscape = o.canEscape,
              cards = viewModel.getCards(),
              cardPainter = { path ->
                rememberPainter(viewModel.assets.card(path), viewModel.reader)
              },
              onChooseFight = { viewModel.battleChooseFight() },
              onChooseEscape = { viewModel.battleChooseEscape() },
              onRoll = { viewModel.battleRoll() },
              onApplyCards = { value, usedCards ->
                viewModel.battleApplyModifier(value, usedCards)
              },
              onContinue = { viewModel.battleContinue() }
            )
            o.postCombatVnText?.let { text ->
              VNTextBox(
                speaker = o.postCombatVnSpeaker,
                text = text,
                onNext = { viewModel.battlePostCombatVnNext() }
              )
            }
          }
        }

        is EngineOutput.ShowSceneView -> Unit

        else -> Text("Загрузка...")
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      contentAlignment = Alignment.TopEnd
    ) {
      VnOutlinedButton(onClick = { gameMenu = GameMenu.Hub }) {
        Text("⚙")
      }
    }

    when (gameMenu) {
      GameMenu.Hub -> {
        GameMenuHub(
          onSaveLoad = { gameMenu = GameMenu.SaveLoad },
          onPlayerStats = { gameMenu = GameMenu.Stats },
          onDismiss = { gameMenu = GameMenu.None }
        )
      }

      GameMenu.SaveLoad -> {
        SaveSlotsMenu(
          viewModel = viewModel,
          onRequestSaveWithScreenshot = { slot -> pendingScreenshotSaveSlot = slot },
          onDismiss = { gameMenu = GameMenu.None }
        )
      }

      GameMenu.Stats -> {
        PlayerStatsScreen(
          viewModel = viewModel,
          refreshKey = viewModel.currentOutput,
          onDismiss = { gameMenu = GameMenu.None }
        )
      }

      GameMenu.None -> Unit
    }
  }
}

private enum class GameMenu {
  None,
  Hub,
  SaveLoad,
  Stats
}

@Composable
private fun SceneNavControl(
  alignment: Alignment,
  link: NavLink,
  assets: AssetPathResolver,
  reader: AssetReader,
  onNavigate: () -> Unit
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = alignment
  ) {
    val pad = Modifier.padding(12.dp)

    val icon = link.icon
    val label = link.label

    when {
      !icon.isNullOrBlank() -> {
        val painter = rememberPainter(assets.image(icon), reader)
        Box(
          modifier = pad
            .size(72.dp)
            .clickable(onClick = onNavigate),
          contentAlignment = Alignment.Center
        ) {
          if (painter != null) {
            Image(
              painter = painter,
              contentDescription = label,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )
          } else {
            Text(
              text = defaultNavGlyph(alignment),
              modifier = Modifier.clickable(onClick = onNavigate)
            )
          }
        }
      }

      !label.isNullOrBlank() -> {
        VnOutlinedButton(
          onClick = onNavigate,
          modifier = pad
        ) {
          Text(text = label)
        }
      }

      else -> {
        VnOutlinedButton(
          onClick = onNavigate,
          modifier = pad.size(56.dp)
        ) {
          Text(text = defaultNavGlyph(alignment))
        }
      }
    }
  }
}

private fun defaultNavGlyph(alignment: Alignment): String = when (alignment) {
  Alignment.TopCenter -> "↑"
  Alignment.BottomCenter -> "↓"
  Alignment.CenterStart -> "←"
  Alignment.CenterEnd -> "→"
  else -> ">"
}

@Composable
fun rememberPainter(
  path: String,
  reader: AssetReader,
): BitmapPainter? {
  var painter by remember(path) { mutableStateOf<BitmapPainter?>(null) }

  LaunchedEffect(path) {
    val bytes = reader.readBytes(path)
    painter = BitmapPainter(loadImageBitmap(bytes.inputStream()))
  }

  return painter
}