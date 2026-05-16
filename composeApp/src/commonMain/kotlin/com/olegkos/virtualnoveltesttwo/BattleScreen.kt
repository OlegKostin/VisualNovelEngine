package com.olegkos.virtualnoveltesttwo

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.vnengine.engine.BattlePhase
import com.olegkos.vnengine.engine.UiCard
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun BattleScreen(
  playerName: String = "",
  title: String,
  monsterName: String,
  monsterImagePainter: BitmapPainter?,
  monsterHp: Int,
  monsterCombatDamage: Int,
  monsterHorrorDamage: Int,
  playerHealth: Int,
  playerSanity: Int,
  phase: BattlePhase,
  diceName: String?,
  sides: Int?,
  difficulty: Int?,
  result: Int?,
  modifier: Float,
  canUseCards: Boolean,
  canEscape: Boolean,
  cards: List<UiCard>,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onChooseFight: () -> Unit,
  onChooseEscape: () -> Unit,
  onRoll: () -> Unit,
  onApplyCards: (Float, List<String>) -> Unit,
  onContinue: () -> Unit
) {
  var selectedCards by remember { mutableStateOf(setOf<String>()) }
  var showCards by remember { mutableStateOf(false) }

  var isRolling by remember { mutableStateOf(false) }
  var rollingValue by remember { mutableIntStateOf(1) }
  var revealResult by remember(result, phase) { mutableStateOf(false) }

  LaunchedEffect(isRolling) {
    if (!isRolling) return@LaunchedEffect

    revealResult = false
    val diceSides = sides ?: 20

    // Как раньше: нарастающая пауза, но с более высокого старта — грани успевают читаться.
    var delayMs = 70L
    repeat(15) {
      rollingValue = (1..diceSides).random()
      delay(delayMs)
      delayMs += 10
    }

    rollingValue = result ?: rollingValue
    isRolling = false
    revealResult = true
  }

  val modifierStat = when (phase) {
    BattlePhase.HORROR -> StatType.fromKey("opt_will")
    BattlePhase.COMBAT, BattlePhase.POST_COMBAT_VN -> StatType.fromKey("opt_str")
    BattlePhase.ESCAPE -> StatType.fromKey("opt_luck")
    else -> null
  }

  val totalResult = result?.let { (it + modifier).toInt() }
  val valueToShow = when {
    isRolling -> rollingValue
    result != null -> result
    else -> 1
  }

  val bonus = cards
    .filter { it.id in selectedCards }
    .sumOf { it.value.toDouble() }
    .toFloat()

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val screenMaxHeight = this.maxHeight
    val screenMaxWidth = this.maxWidth
    val fontSize = (maxHeight.value * 0.045f).sp
    val panelPadding = 12.dp
    val modIconSize = (screenMaxWidth * 0.05f).coerceAtLeast(20.dp).coerceAtMost(34.dp)
    val monsterImageSize = (screenMaxWidth * 0.22f).coerceAtMost(screenMaxHeight * 0.45f)
    val diceSize = (screenMaxHeight * 0.70f)
      .coerceAtLeast(180.dp)
      .coerceAtMost(screenMaxWidth * 0.70f)
    val cardSize = screenMaxWidth * 0.30f

    val playerBase = Color(0x1A4CAF50)
    val centerBase = Color(0x1A2196F3)
    val monsterBase = Color(0x1AF44336)

    val activeGlow = Color.White.copy(alpha = 0.28f)
    val inactiveBorder = Color.White.copy(alpha = 0.08f)

    val playerActive = false
    val centerActive = phase == BattlePhase.HORROR ||
        phase == BattlePhase.COMBAT ||
        phase == BattlePhase.ESCAPE ||
        phase == BattlePhase.ACTION ||
        phase == BattlePhase.POST_COMBAT_VN
    val monsterActive = phase == BattlePhase.COMBAT ||
        phase == BattlePhase.RESOLVE ||
        phase == BattlePhase.POST_COMBAT_VN

    Row(modifier = Modifier.fillMaxSize()) {

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(panelPadding / 2)
          .background(playerBase, RoundedCornerShape(14.dp))
          .border(
            width = if (playerActive) 1.5.dp else 1.dp,
            color = if (playerActive) activeGlow else inactiveBorder,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(panelPadding),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (playerName.isNotBlank()) {
          Text(
            text = playerName,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(Modifier.height(10.dp))
        }

        IconStatRow(
          stat = StatType.fromKey("health"),
          count = playerHealth,
          fontSize = fontSize
        )

        Spacer(Modifier.height(12.dp))

        IconStatRow(
          stat = StatType.fromKey("mental_health"),
          count = playerSanity,
          fontSize = fontSize
        )

        Spacer(Modifier.height(12.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val modStat = modifierStat
          if (modStat != null) {
            Image(
              painter = painterResource(modStat.image),
              contentDescription = null,
              modifier = Modifier.size(modIconSize)
            )
          }
          Text(modifier.toString(), fontSize = fontSize)
        }

        Spacer(Modifier.height(10.dp))
        Text(
          "Бросок: ${if (result != null && revealResult) totalResult else "-"}",
          fontSize = fontSize
        )

        Spacer(Modifier.height(12.dp))

        if (showCards) {
          Text("Карты", fontSize = fontSize)
          Spacer(Modifier.height(6.dp))

          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
              .fillMaxWidth()
              .height(screenMaxHeight * 0.70f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(cards, key = { it.id }) { card ->
              val isSelected = card.id in selectedCards
              val painter = cardPainter(card.image)
              val tickSize = (fontSize.value * 1.2f).sp

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                  selectedCards =
                    if (isSelected) selectedCards - card.id else selectedCards + card.id
                }
              ) {
                painter?.let {
                  Box {
                    Image(
                      painter = it,
                      contentDescription = null,
                      modifier = Modifier.size(cardSize)
                    )
                    if (isSelected) {
                      Text(
                        "✓",
                        fontSize = tickSize,
                        modifier = Modifier.align(Alignment.TopEnd)
                      )
                    }
                  }
                }
                Text("+${card.value}", fontSize = fontSize)
              }
            }
          }
        }
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(panelPadding / 2)
          .background(centerBase, RoundedCornerShape(14.dp))
          .border(
            width = if (centerActive) 1.5.dp else 1.dp,
            color = if (centerActive) activeGlow else inactiveBorder,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(panelPadding),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(title, textAlign = TextAlign.Center, fontSize = fontSize)
        Spacer(Modifier.height(8.dp))
        Text(diceName ?: "Проверка", fontSize = fontSize)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(screenMaxHeight * 0.70f),
          contentAlignment = Alignment.TopCenter
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            Crossfade(
              targetState = valueToShow,
              animationSpec = tween(durationMillis = 95, easing = LinearEasing),
              label = "battleDiceFace"
            ) { face ->
              Image(
                painter = diceFacePainter(face),
                contentDescription = null,
                modifier = Modifier.size(diceSize)
              )
            }
            Spacer(Modifier.height(8.dp))
            Text("d${sides ?: "-"}", fontSize = fontSize)
            Text("Сложность: ${difficulty ?: "-"}", fontSize = fontSize)
            if (isRolling) {
              Text("Бросок...", fontSize = fontSize)
            } else if (result != null && revealResult) {
              Text("Готово", fontSize = fontSize)
            }
          }
        }

        Spacer(Modifier.height(8.dp))

        when (phase) {
          BattlePhase.ACTION -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              VnOutlinedButton(onClick = onChooseFight) {
                Text("Сражаться", fontSize = fontSize)
              }
              if (canEscape) {
                VnOutlinedButton(onClick = onChooseEscape) {
                  Text("Сбежать", fontSize = fontSize)
                }
              }
            }
          }

          BattlePhase.HORROR, BattlePhase.COMBAT, BattlePhase.ESCAPE -> {
            if (result == null) {
              VnOutlinedButton(
                enabled = !isRolling,
                onClick = {
                  onRoll()
                  isRolling = true
                }
              ) {
                Text("Бросить", fontSize = fontSize)
              }
            } else if (isRolling || !revealResult) {
              VnOutlinedButton(enabled = false, onClick = {}) {
                Text("Считаем...", fontSize = fontSize)
              }
            } else {
              if (canUseCards && !showCards) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  VnOutlinedButton(onClick = { onApplyCards(0f, emptyList()) }) {
                    Text("Без карт", fontSize = fontSize)
                  }
                  VnOutlinedButton(onClick = {
                    selectedCards = emptySet()
                    showCards = true
                  }) {
                    Text("Карты", fontSize = fontSize)
                  }
                }
              } else if (showCards) {
                VnOutlinedButton(
                  onClick = {
                    if (selectedCards.isEmpty()) {
                      onApplyCards(0f, emptyList())
                    } else {
                      onApplyCards(bonus, selectedCards.toList())
                    }
                    selectedCards = emptySet()
                    showCards = false
                  }
                ) {
                  Text(
                    if (selectedCards.isEmpty()) "Применить"
                    else "Применить (+$bonus)",
                    fontSize = fontSize
                  )
                }
              } else {
                VnOutlinedButton(onClick = onContinue) {
                  Text("Продолжить", fontSize = fontSize)
                }
              }
            }
          }

          BattlePhase.POST_COMBAT_VN -> {
            Spacer(Modifier.height(8.dp))
          }

          else -> {
            VnOutlinedButton(onClick = onContinue) {
              Text("Продолжить", fontSize = fontSize)
            }
          }
        }
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(panelPadding / 2)
          .background(monsterBase, RoundedCornerShape(14.dp))
          .border(
            width = if (monsterActive) 1.5.dp else 1.dp,
            color = if (monsterActive) activeGlow else inactiveBorder,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(panelPadding),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(monsterName, textAlign = TextAlign.Center, fontSize = fontSize)
        Spacer(Modifier.height(10.dp))

        monsterImagePainter?.let {
          Image(
            painter = it,
            contentDescription = monsterName,
            modifier = Modifier
              .width(monsterImageSize)
              .height(monsterImageSize)
          )
        }

        Spacer(Modifier.height(12.dp))

        IconStatRow(
          stat = StatType.HP,
          count = monsterHp,
          fontSize = fontSize
        )

        Spacer(Modifier.height(12.dp))

        IconStatRow(
          stat = StatType.MENTAL,
          count = monsterHorrorDamage,
          fontSize = fontSize
        )

        Spacer(Modifier.height(12.dp))

        IconStatRow(
          stat = StatType.STR,
          count = monsterCombatDamage,
          fontSize = fontSize
        )
      }
    }
  }
}

enum class IconStatLayout {
  /** Ровный ряд с лёгким джиттером — бой, модификаторы. */
  Linear,
  /** Здоровье / рассудок: мало иконок — к центру, больше — шире по области. */
  CenterCluster
}

private data class ScatterToken(
  val nx: Float,
  val ny: Float,
  val rotationDeg: Float
)

private data class FallingEntry(
  val id: Int,
  val slotIndex: Int,
  val fromCount: Int
)

private fun scatterForSlot(stat: StatType, slotIndex: Int): ScatterToken {
  var s = (slotIndex * 1_009 + stat.hashCode() * 9_176 + 7_917) and 0x7FFFFFFF
  fun next(): Int {
    s = (s * 1_103_515_245 + 12_345) and 0x7FFFFFFF
    return s
  }
  val nx = (next() % 2001) / 1000f - 1f
  val ny = (next() % 2001) / 1000f - 1f
  val rot = ((next() % 81) - 40).toFloat()
  return ScatterToken(nx, ny, rot)
}

/** Чем меньше иконок, тем ближе к центру области. */
private fun horizontalSpreadFactor(tokenCount: Int): Float = when (tokenCount.coerceIn(1, 8)) {
  1 -> 0f
  2 -> 0.22f
  3 -> 0.34f
  4 -> 0.46f
  5 -> 0.58f
  6 -> 0.70f
  7 -> 0.84f
  else -> 1f
}

private fun computeLinearOffset(
  slotIndex: Int,
  tokenCount: Int,
  widthPx: Dp,
  tokenSize: Dp,
  scatter: ScatterToken
): Triple<Dp, Dp, Float> {
  val capped = tokenCount.coerceIn(1, 8)
  val sidePad = 2.dp
  val center = widthPx / 2
  val preferredStep = tokenSize * 0.68f
  val maxStep = if (capped <= 1) {
    0.dp
  } else {
    (widthPx - tokenSize - sidePad * 2) / (capped - 1)
  }
  val step = if (capped <= 1) 0.dp else minOf(preferredStep, maxStep)
  val rowH = tokenSize * 1.75f
  val baselineY = rowH - tokenSize - 4.dp
  val offsetFromCenter = (slotIndex - (capped - 1) / 2f) * step
  val iconCenterX = center + offsetFromCenter
  val baseX = iconCenterX - tokenSize / 2
  val jitterXMax = kotlin.math.min(14f, step.value * 0.40f).coerceAtLeast(2f).dp
  val jitterYMax = kotlin.math.min(24f, tokenSize.value * 0.42f).dp
  val dx = jitterXMax * scatter.nx
  val dy = jitterYMax * scatter.ny
  return Triple(baseX + dx, baselineY + dy, scatter.rotationDeg)
}

private fun computeCenterClusterOffset(
  slotIndex: Int,
  tokenCount: Int,
  widthPx: Dp,
  tokenSize: Dp,
  scatter: ScatterToken
): Triple<Dp, Dp, Float> {
  val capped = tokenCount.coerceIn(1, 8)
  val sidePad = 2.dp
  val center = widthPx / 2
  val rowH = tokenSize * 1.85f
  val baselineY = rowH - tokenSize - 4.dp

  val spread = horizontalSpreadFactor(capped)
  val maxHalfSpan = ((widthPx - tokenSize - sidePad * 2) / 2) * spread

  val along = if (capped == 1) {
    0f
  } else {
    (slotIndex / (capped - 1f) - 0.5f) * 2f
  }

  val iconCenterX = center + maxHalfSpan * along
  val baseX = iconCenterX - tokenSize / 2

  val jitterXMax = (6.dp + maxHalfSpan * 0.28f).coerceAtMost(16.dp)
  val jitterYMax = (5.dp + tokenSize * 0.10f * (0.35f + spread * 0.65f)).coerceAtMost(20.dp)
  val dx = jitterXMax * scatter.nx
  val dy = jitterYMax * scatter.ny

  return Triple(baseX + dx, baselineY + dy, scatter.rotationDeg)
}

private fun computeIconOffset(
  layout: IconStatLayout,
  slotIndex: Int,
  tokenCount: Int,
  widthPx: Dp,
  tokenSize: Dp,
  stat: StatType
): Triple<Dp, Dp, Float> {
  val scatter = scatterForSlot(stat, slotIndex)
  return when (layout) {
    IconStatLayout.Linear -> computeLinearOffset(slotIndex, tokenCount, widthPx, tokenSize, scatter)
    IconStatLayout.CenterCluster ->
      computeCenterClusterOffset(slotIndex, tokenCount, widthPx, tokenSize, scatter)
  }
}

@Composable
internal fun IconStatRow(
  stat: StatType?,
  count: Int,
  fontSize: TextUnit,
  layout: IconStatLayout = IconStatLayout.Linear
) {
  val capped = count.coerceIn(0, 8)

  if (stat == null) {
    Text(capped.toString(), fontSize = fontSize)
    return
  }

  val rowStat: StatType = stat

  val chipPainter = painterResource(rowStat.image)

  val falling = remember(rowStat) { mutableStateListOf<FallingEntry>() }
  var fallingIdSeq by remember(rowStat) { mutableIntStateOf(0) }
  var lastCapped by remember(rowStat) { mutableIntStateOf(-1) }

  LaunchedEffect(capped) {
    if (lastCapped == -1) {
      lastCapped = capped
      return@LaunchedEffect
    }
    val prev = lastCapped
    if (capped < prev) {
      val old = prev.coerceIn(0, 8)
      val new = capped.coerceIn(0, 8)
      if (old > 0 && new < old) {
        for (slot in new until old) {
          val id = fallingIdSeq
          fallingIdSeq = fallingIdSeq + 1
          falling.add(FallingEntry(id = id, slotIndex = slot, fromCount = old))
        }
      }
    }
    lastCapped = capped
  }

  if (capped == 0 && falling.isEmpty()) {
    Text("0", fontSize = fontSize)
    return
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 2.dp)
  ) {
    val widthPx = maxWidth
    val tokenSize = (widthPx * 0.38f).coerceIn(42.dp, 92.dp)
    val rowH = tokenSize * 1.75f

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(rowH)
    ) {
      repeat(capped) { idx ->
        val (baseX, baseY, rot) = computeIconOffset(
          layout = layout,
          slotIndex = idx,
          tokenCount = capped,
          widthPx = widthPx,
          tokenSize = tokenSize,
          stat = rowStat
        )

        Box(
          modifier = Modifier
            .offset(x = baseX, y = baseY)
            .graphicsLayer {
              rotationZ = rot
              transformOrigin = TransformOrigin(0.5f, 0.92f)
            }
            .size(tokenSize)
        ) {
          Image(
            painter = chipPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
          )
        }
      }

      falling.forEach { entry ->
        key(entry.id) {
          FallingStatChip(
            stat = rowStat,
            chipPainter = chipPainter,
            entry = entry,
            layout = layout,
            widthPx = widthPx,
            tokenSize = tokenSize,
            onFinished = { falling.removeAll { it.id == entry.id } }
          )
        }
      }
    }
  }
}

@Composable
private fun FallingStatChip(
  stat: StatType,
  chipPainter: Painter,
  entry: FallingEntry,
  layout: IconStatLayout,
  widthPx: Dp,
  tokenSize: Dp,
  onFinished: () -> Unit
) {
  val (baseX, baseY, baseRot) = remember(entry.id, entry.fromCount, entry.slotIndex, widthPx, tokenSize, layout) {
    computeIconOffset(layout, entry.slotIndex, entry.fromCount, widthPx, tokenSize, stat)
  }
  val progress = remember(entry.id) { Animatable(0f) }
  LaunchedEffect(entry.id) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing)
    )
    onFinished()
  }
  val fall = (tokenSize.value * 0.55f + progress.value * 140f).dp
  Box(
    modifier = Modifier
      .offset(x = baseX, y = baseY + fall)
      .graphicsLayer {
        alpha = 1f - progress.value
        rotationZ = baseRot + progress.value * 42f
        transformOrigin = TransformOrigin(0.5f, 0.92f)
      }
      .size(tokenSize)
  ) {
    Image(
      painter = chipPainter,
      contentDescription = null,
      modifier = Modifier.fillMaxSize()
    )
  }
}