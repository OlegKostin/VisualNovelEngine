package com.olegkos.virtualnoveltesttwo

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.vnengine.engine.BattlePhase
import com.olegkos.vnengine.engine.UiCard
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun BattleScreen(
  title: String,
  monsterName: String,
  monsterImagePainter: BitmapPainter?,
  monsterHp: Int,
  monsterMaxHp: Int,
  monsterAttack: Int,
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
    var delayMs = 40L

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
    BattlePhase.COMBAT -> StatType.fromKey("opt_str")
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
    val iconSize = (screenMaxWidth * 0.05f).coerceAtLeast(20.dp).coerceAtMost(34.dp)
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
        phase == BattlePhase.ACTION
    val monsterActive = phase == BattlePhase.COMBAT || phase == BattlePhase.RESOLVE

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

        Text("Здоровье", fontSize = fontSize)
        IconStatRow(
          stat = StatType.fromKey("health"),
          count = playerHealth,
          iconSize = iconSize,
          fontSize = fontSize
        )

        Spacer(Modifier.height(10.dp))

        Text("Рассудок", fontSize = fontSize)
        IconStatRow(
          stat = StatType.fromKey("mental_health"),
          count = playerSanity,
          iconSize = iconSize,
          fontSize = fontSize
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          modifierStat?.let {
            Image(
              painter = painterResource(it.image),
              contentDescription = null,
              modifier = Modifier.size(iconSize)
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
            Image(
              painter = painterResource(diceImage(valueToShow)),
              contentDescription = null,
              modifier = Modifier.size(diceSize)
            )
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
              Button(onClick = onChooseFight) {
                Text("Сражаться", fontSize = fontSize)
              }
              if (canEscape) {
                Button(onClick = onChooseEscape) {
                  Text("Сбежать", fontSize = fontSize)
                }
              }
            }
          }

          BattlePhase.HORROR, BattlePhase.COMBAT, BattlePhase.ESCAPE -> {
            if (result == null) {
              Button(
                enabled = !isRolling,
                onClick = {
                  onRoll()
                  isRolling = true
                }
              ) {
                Text("Бросить", fontSize = fontSize)
              }
            } else if (isRolling || !revealResult) {
              Button(enabled = false, onClick = {}) {
                Text("Считаем...", fontSize = fontSize)
              }
            } else {
              if (canUseCards && !showCards) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(onClick = { onApplyCards(0f, emptyList()) }) {
                    Text("Без карт", fontSize = fontSize)
                  }
                  Button(onClick = {
                    selectedCards = emptySet()
                    showCards = true
                  }) {
                    Text("Карты", fontSize = fontSize)
                  }
                }
              } else if (showCards) {
                Button(
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
                Button(onClick = onContinue) {
                  Text("Продолжить", fontSize = fontSize)
                }
              }
            }
          }

          else -> {
            Button(onClick = onContinue) {
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
        Text("Монстр", fontSize = fontSize)
        Spacer(Modifier.height(8.dp))
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
        Text("HP: $monsterHp / $monsterMaxHp", fontSize = fontSize)
        Text("Сила атаки: $monsterAttack", fontSize = fontSize)
      }
    }
  }
}

@Composable
private fun IconStatRow(
  stat: StatType?,
  count: Int,
  iconSize: Dp,
  fontSize: TextUnit
) {
  val safeCount = count.coerceAtLeast(0).coerceAtMost(20)

  if (safeCount == 0 || stat == null) {
    Text(safeCount.toString(), fontSize = fontSize)
    return
  }

  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(safeCount) {
      Image(
        painter = painterResource(stat.image),
        contentDescription = null,
        modifier = Modifier.size(iconSize)
      )
    }
  }
}