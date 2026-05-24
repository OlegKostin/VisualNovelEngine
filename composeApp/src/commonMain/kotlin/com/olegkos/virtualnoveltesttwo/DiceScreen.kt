package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.engine.DicePhase
import com.olegkos.vnengine.engine.UiCard
import kotlinx.coroutines.delay

@Composable
fun DiceScreen(
  name: String,
  sides: Int,
  result: Int?,
  modifier: Float,
  difficulty: Int,
  cards: List<UiCard>,
  cardPainter: @Composable (String) -> BitmapPainter?,
  phase: DicePhase,
  onRoll: () -> Unit,
  onApplyCard: (Float, List<String>) -> Unit,
  onContinue: () -> Unit
) {

  var isRolling by remember { mutableStateOf(false) }
  var rollingValue by remember { mutableIntStateOf(1) }

  var selectedCards by remember { mutableStateOf(setOf<String>()) }
  var showCards by remember { mutableStateOf(false) }

  // 🎲 анимация броска
  LaunchedEffect(isRolling) {
    if (!isRolling) return@LaunchedEffect

    var delayMs = 40L

    repeat(15) {
      rollingValue = (1..sides).random()
      delay(delayMs)
      delayMs += 10
    }

    rollingValue = result ?: 1
    isRolling = false
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    val diceSize = maxHeight * 0.35f

    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {

      Text("Проверка: $name d$sides")

      Spacer(Modifier.height(24.dp))

      val valueToShow = when {
        isRolling -> rollingValue
        result != null -> result
        else -> 1
      }

      val dicePainter = diceFacePainter(valueToShow)

      Image(
        painter = dicePainter,
        contentDescription = null,
        modifier = Modifier.size(diceSize),
      )

      Spacer(Modifier.height(24.dp))

      val uiPhase = when {
        showCards -> DicePhase.CARD_SELECTION
        else -> phase
      }

      when (uiPhase) {

      DicePhase.ROLL -> {
        VnOutlinedButton(onClick = {
          onRoll()
          isRolling = true
        }) {
          Text("Бросить")
        }
      }

      DicePhase.RESULT -> {
        if (!isRolling) {

          Text("Бросок: $result")
          Text("Модификатор: $modifier")

          Spacer(Modifier.height(16.dp))

          VnOutlinedButton(onClick = {
            onApplyCard(0f, emptyList())
          }) {
            Text("Без карт")
          }

          Spacer(Modifier.height(8.dp))

          VnOutlinedButton(onClick = {
            selectedCards = emptySet()
            showCards = true
          }) {
            Text("Использовать карты")
          }
        }
      }

      DicePhase.FINAL -> {

        val total = (result ?: 0) + modifier

        Text("Бросок: $result")
        Text("Итоговый модификатор: $modifier")
        Text("Итого: $total / $difficulty")

        Spacer(Modifier.height(16.dp))

        VnOutlinedButton(onClick = onContinue) {
          Text("Продолжить")
        }
      }

      DicePhase.CARD_SELECTION -> {

        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {

          Text("Выбери карты")

          Spacer(Modifier.height(16.dp))

          BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
          ) {
            val cardSize = maxWidth * 0.15f

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {

              cards.forEach { card ->

                val isSelected = card.id in selectedCards
                val painter = cardPainter(card.image)

                painter?.let {

                  Column(
                    modifier = Modifier
                      .padding(6.dp)
                      .clickable {
                        selectedCards = if (isSelected) {
                          selectedCards - card.id
                        } else {
                          selectedCards + card.id
                        }
                      },
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {

                    Box {

                      Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(cardSize)
                      )

                      if (isSelected) {
                        Text(
                          text = "✓",
                          modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                        )
                      }
                    }

                    Text("+${card.value}")
                  }
                }
              }
            }
          }

          Spacer(Modifier.height(20.dp))

          val bonus = cards
            .filter { it.id in selectedCards }
            .sumOf { it.value.toDouble() }
            .toFloat()

          VnOutlinedButton(
            enabled = selectedCards.isNotEmpty(),
            onClick = {
              val usedCards = selectedCards.toList()

              onApplyCard(bonus, usedCards)

              selectedCards = emptySet()
              showCards = false
            }
          ) {
            Text("Применить (+$bonus)")
          }
        }
      }
      }
    }
  }
}