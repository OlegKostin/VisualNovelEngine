package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
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

  val scroll = rememberScrollState()

  LaunchedEffect(isRolling) {
    if (!isRolling) return@LaunchedEffect

    var delayMs = 40L
    val diceSides = sides ?: 20

    repeat(15) {
      rollingValue = (1..diceSides).random()
      delay(delayMs)
      delayMs += 10
    }

    rollingValue = result ?: rollingValue
    isRolling = false
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(scroll)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(title)
    Spacer(Modifier.height(8.dp))
    Text("Монстр: $monsterName")
    Text("HP монстра: $monsterHp / $monsterMaxHp")
    Text("Твое здоровье: $playerHealth")
    Text("Твой рассудок: $playerSanity")

    Spacer(Modifier.height(12.dp))

    monsterImagePainter?.let {
      Image(
        painter = it,
        contentDescription = monsterName,
        modifier = Modifier.size(220.dp)
      )
      Spacer(Modifier.height(12.dp))
    }

    when (phase) {
      BattlePhase.ACTION -> {
        Text("Выбери действие")
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = onChooseFight) {
            Text("Сражаться")
          }

          if (canEscape) {
            Button(onClick = onChooseEscape) {
              Text("Сбежать")
            }
          }
        }
      }

      BattlePhase.HORROR, BattlePhase.COMBAT, BattlePhase.ESCAPE -> {
        val titleText = when (phase) {
          BattlePhase.HORROR -> "Фаза ужаса"
          BattlePhase.COMBAT -> "Фаза боя"
          BattlePhase.ESCAPE -> "Фаза побега"
          else -> ""
        }

        val valueToShow = when {
          isRolling -> rollingValue
          result != null -> result
          else -> 1
        }

        Text(titleText)
        Spacer(Modifier.height(6.dp))
        Text(diceName ?: "Проверка")
        Text("d${sides ?: "-"}  сложность: ${difficulty ?: "-"}")
        Text("Модификатор: $modifier")
        Text("Результат: ${result ?: "-"}")

        Spacer(Modifier.height(12.dp))

        Image(
          painter = painterResource(diceImage(valueToShow)),
          contentDescription = null,
          modifier = Modifier.size(160.dp)
        )

        Spacer(Modifier.height(12.dp))

        if (result == null) {
          Button(
            enabled = !isRolling,
            onClick = {
              onRoll()
              isRolling = true
            }
          ) {
            Text("Бросить")
          }
        } else {
          if (canUseCards && !showCards) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(onClick = { onApplyCards(0f, emptyList()) }) {
                Text("Без карт")
              }
              Button(onClick = {
                selectedCards = emptySet()
                showCards = true
              }) {
                Text("Использовать карты")
              }
            }
          } else if (showCards) {
            Text("Выбери карты")
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              cards.forEach { card ->
                val isSelected = card.id in selectedCards
                val painter = cardPainter(card.image)

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.clickable {
                    selectedCards = if (isSelected) selectedCards - card.id else selectedCards + card.id
                  }
                ) {
                  painter?.let {
                    Box {
                      Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(88.dp)
                      )
                      if (isSelected) {
                        Text("✓", modifier = Modifier.align(Alignment.TopEnd))
                      }
                    }
                  }
                  Text("+${card.value}")
                }
              }
            }

            Spacer(Modifier.height(8.dp))

            val bonus = cards
              .filter { it.id in selectedCards }
              .sumOf { it.value.toDouble() }
              .toFloat()

            Button(
              enabled = selectedCards.isNotEmpty(),
              onClick = {
                onApplyCards(bonus, selectedCards.toList())
                selectedCards = emptySet()
                showCards = false
              }
            ) {
              Text("Применить (+$bonus)")
            }
          } else {
            Button(onClick = onContinue) {
              Text("Продолжить")
            }
          }
        }
      }

      BattlePhase.RESOLVE -> {
        Text("Рассчет результата...")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onContinue) {
          Text("Продолжить")
        }
      }

      else -> {
        Button(onClick = onContinue) {
          Text("Продолжить")
        }
      }
    }
  }
}