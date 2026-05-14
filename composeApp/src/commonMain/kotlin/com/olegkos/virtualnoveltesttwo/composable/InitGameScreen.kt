package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.variables.forStatPreview
import com.olegkos.vnengine.scene.SubClass
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InitGameScreen(
  classes: List<SubClass.GameClass>,
  cardPainter: @Composable (String) -> Painter?,
  resolveStartingCardPreview: (SubClass.ClassStartingCard) -> Pair<Int, String>?,
  onConfirm: (String, SubClass.GameClass?) -> Unit
) {

  var name by remember { mutableStateOf("") }
  var selectedClass by remember { mutableStateOf<SubClass.GameClass?>(null) }
  var hoveredClassId by remember { mutableStateOf<String?>(null) }

  BoxWithConstraints(
    Modifier
      .fillMaxSize()
      .background(Color(0xFFE9ECF3))
      .padding(16.dp)
  ) {
    val statIconSize = maxWidth * 0.05f

    Column(
      Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

    Text("Создание персонажа", style = MaterialTheme.typography.headlineMedium)

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Имя") }
      )

      Spacer(Modifier.width(12.dp))

      VnOutlinedButton(
        onClick = { onConfirm(name, selectedClass) },
        enabled = name.isNotBlank() && selectedClass != null
      ) {
        Text("Начать")
      }
    }

    Spacer(Modifier.height(24.dp))

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {

      classes.take(3).forEachIndexed { index, cls ->

        val isHovered = hoveredClassId == cls.id
        val isSelected = selectedClass?.id == cls.id

        val scale by animateFloatAsState(
          targetValue = if (isSelected) 1.05f else if (isHovered) 1.02f else 1f,
          animationSpec = spring(dampingRatio = 0.75f),
          label = ""
        )

        val selectedColor = when (index) {
          0 -> Color(0xFF6FAF73)
          1 -> Color(0xFF7B86C2)
          else -> Color(0xFFC07C7C)
        }

        val bg = if (isSelected) selectedColor else Color(0xFFC9D6F5)

        val brush = Brush.verticalGradient(
          listOf(bg.copy(0.95f), bg.copy(0.65f))
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .padding(8.dp)
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
              transformOrigin = TransformOrigin(0.5f, 0.5f)
              clip = false
            }
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .background(brush, RoundedCornerShape(14.dp))
            .pointerMoveFilter(
              onEnter = {
                hoveredClassId = cls.id
                false
              },
              onExit = {
                hoveredClassId = null
                false
              }
            )
            .cursorForHand()
            .clickable {
              selectedClass = if (selectedClass?.id == cls.id) null else cls
            }
        ) {

          Column(
            Modifier
              .fillMaxSize()
              .padding(14.dp)
              .graphicsLayer { clip = false },
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {

            // =======================
            // TITLE
            // =======================
            Text(cls.name, style = MaterialTheme.typography.titleLarge)

            // =======================
            // DESCRIPTION (JSON: description)
            // =======================
            Text(
              text = cls.description.ifBlank {
                "Описание не задано в JSON (поле description у класса)."
              },
              style = MaterialTheme.typography.bodyMedium
            )

            // =======================
            // STATS (2x3 GRID)
            // =======================
            StatsBlock(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
              cls = cls,
              statIconSize = statIconSize
            )

            // =======================
            // STARTING CARDS (JSON: startingCards)
            // =======================
            ClassStartingCardsRow(
              classId = cls.id,
              startingCards = cls.startingCards,
              resolvePreview = resolveStartingCardPreview,
              cardPainter = cardPainter
            )

            Text(
              if (isSelected) "Выбрано (клик снова — снять)" else "Клик для выбора"
            )
          }
        }
      }
    }
    }
  }
}

@Composable
private fun ClassStartingCardsRow(
  classId: String,
  startingCards: List<SubClass.ClassStartingCard>,
  resolvePreview: (SubClass.ClassStartingCard) -> Pair<Int, String>?,
  cardPainter: @Composable (String) -> Painter?
) {
  Column(
    Modifier
      .fillMaxWidth()
      .padding(top = 8.dp)
  ) {
    Text(
      text = "Стартовые карты",
      style = MaterialTheme.typography.labelMedium
    )
    Spacer(Modifier.height(6.dp))
    if (startingCards.isEmpty()) {
      Text(
        text = "В JSON класса задайте startingCards — как drawCard: random, value или image.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF5C6575)
      )
      return@Column
    }
    val gap = 8.dp
    val slotsVisible = 3
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
      Modifier
        .fillMaxWidth()
        .background(Color(0x18000000), RoundedCornerShape(10.dp))
        .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
      val cardWidth =
        ((maxWidth - gap * (slotsVisible - 1)) / slotsVisible).coerceAtLeast(28.dp)
      val rowHeight = cardWidth * (3.5f / 2.5f)

      Row(
        modifier = Modifier
          .height(rowHeight)
          .pointerInput(scrollState) {
            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                var wheel = 0f
                event.changes.forEach { change ->
                  val d = change.scrollDelta
                  if (d != Offset.Zero) {
                    wheel += d.y + d.x
                    change.consume()
                  }
                }
                if (wheel != 0f) {
                  scope.launch {
                    scrollState.scroll {
                      scrollBy(-wheel)
                    }
                  }
                }
              }
            }
          }
          .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
      ) {
        startingCards.forEachIndexed { index, spec ->
          StartingCardPreviewSlot(
            modifier = Modifier
              .width(cardWidth)
              .height(rowHeight),
            classId = classId,
            slotIndex = index,
            spec = spec,
            resolvePreview = resolvePreview,
            cardPainter = cardPainter
          )
        }
      }
    }
  }
}

@Composable
private fun StartingCardPreviewSlot(
  modifier: Modifier,
  classId: String,
  slotIndex: Int,
  spec: SubClass.ClassStartingCard,
  resolvePreview: (SubClass.ClassStartingCard) -> Pair<Int, String>?,
  cardPainter: @Composable (String) -> Painter?
) {
  val specSignature = buildString {
    append(spec.random?.toString() ?: "n")
    append('|')
    append(spec.value?.toString() ?: "n")
    append('|')
    append(spec.image ?: "n")
  }
  val preview = remember(classId, slotIndex, specSignature) {
    resolvePreview(spec)
  }
  Box(
    modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF2E3A59))
  ) {
    if (preview != null) {
      val (value, path) = preview
      val p = cardPainter(path)
      if (p != null) {
        Image(
          painter = p,
          contentDescription = null,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 2.dp)
        )
      } else {
        Box(
          Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "+$value",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
          )
        }
      }
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "+$value",
          color = Color.White,
          style = MaterialTheme.typography.labelSmall
        )
      }
    } else {
      Column(
        Modifier
          .fillMaxSize()
          .padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = when {
            spec.random == true -> "?"
            spec.value != null -> "×"
            spec.image != null -> "×"
            else -> "—"
          },
          color = Color.White,
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = when {
            spec.random == true -> "Случайная"
            spec.value != null -> "value=${spec.value}"
            spec.image != null -> spec.image!!.take(12) + if ((spec.image!!.length) > 12) "…" else ""
            else -> "пусто"
          },
          color = Color(0xFFCCCCCC),
          style = MaterialTheme.typography.labelSmall,
          maxLines = 2
        )
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StatsBlock(
  modifier: Modifier = Modifier,
  cls: SubClass.GameClass,
  statIconSize: Dp
) {
  var hoveredStatKey by remember { mutableStateOf<String?>(null) }

  val statPulseTransition = rememberInfiniteTransition(label = "statIconPulse")
  val statPulse by statPulseTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "statPulseWave"
  )

  val tooltipMaxWidth = 168.dp
  val tooltipOffset = 6.dp

  val statRowGap = statIconSize * 0.35f + 6.dp
  val iconTextGap = statIconSize * 0.2f + 4.dp
  val iconTrackWidth = tooltipMaxWidth + tooltipOffset + statIconSize
  val cellMinWidth = iconTrackWidth + iconTextGap + 28.dp
  val cellMaxWidth = iconTrackWidth + iconTextGap + 120.dp

  Box(
    modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .padding(horizontal = 4.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

      cls.stats.entries.chunked(2).take(3).forEach { row ->

        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(statRowGap, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically
        ) {

          row.forEachIndexed { colIndex, (key, value) ->

            val stat = StatType.fromKey(key)
            val statHovered = hoveredStatKey == key

            val pulseFactor = if (statHovered) statPulse else 1f

            val hoverScale by animateFloatAsState(
              targetValue = if (statHovered) 1.06f else 1f,
              animationSpec = spring(dampingRatio = 0.72f),
              label = "statHover"
            )

            val combinedScale = pulseFactor * hoverScale

            Column(
              modifier = Modifier
                .widthIn(min = cellMinWidth, max = cellMaxWidth)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .graphicsLayer { clip = false },
              horizontalAlignment = Alignment.CenterHorizontally
            ) {

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {

                stat?.let {
                  Box(
                    Modifier
                      .width(iconTrackWidth)
                      .height(statIconSize)
                      .graphicsLayer { clip = false }
                      .pointerMoveFilter(
                        onEnter = {
                          hoveredStatKey = key
                          false
                        },
                        onExit = {
                          if (hoveredStatKey == key) hoveredStatKey = null
                          false
                        }
                      )
                      .cursorForHand()
                  ) {
                    Image(
                      painter = painterResource(it.image),
                      contentDescription = key,
                      modifier = Modifier
                        .align(
                          if (colIndex == 0) Alignment.CenterEnd else Alignment.CenterStart
                        )
                        .size(statIconSize)
                        .graphicsLayer {
                          scaleX = combinedScale
                          scaleY = combinedScale
                          transformOrigin = TransformOrigin(0.5f, 0.5f)
                          clip = false
                        }
                    )

                    if (statHovered) {
                      val hint = StatType.hoverHintForKey(key)
                      Surface(
                        color = Color(0xE6FFFFFF),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                          .widthIn(max = tooltipMaxWidth)
                          .align(Alignment.CenterStart)
                          .offset(
                            x = if (colIndex == 0) 0.dp else statIconSize + tooltipOffset,
                            y = 0.dp
                          )
                      ) {
                        Text(
                          text = hint,
                          style = MaterialTheme.typography.bodySmall,
                          color = Color(0xFF2A3142),
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                      }
                    }
                  }
                  Spacer(Modifier.width(iconTextGap))
                }

                Text(
                  text = value.forStatPreview(),
                  style = MaterialTheme.typography.headlineSmall
                )
              }
            }
          }
        }
      }
    }
  }
}
private fun Modifier.cursorForHand(): Modifier {
  return pointerHoverIcon(
    PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
  )
}