package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.engine.DiceDuelPhase
import com.olegkos.vnengine.engine.UiCard

@Composable
fun DiceDuelScreen(
  title: String,
  sides: Int,
  playerName: String?,
  playerModifier: Float,
  playerRoll: Int?,
  playerTotal: Float?,
  opponentName: String,
  opponentImagePainter: BitmapPainter?,
  opponentModifier: Float,
  opponentRoll: Int?,
  opponentTotal: Float?,
  phase: DiceDuelPhase,
  cards: List<UiCard>,
  canUseCards: Boolean,
  resultText: String?,
  cardPainter: @Composable (String) -> BitmapPainter?,
  onRoll: () -> Unit,
  onApplyCards: (Float, List<String>) -> Unit,
  onContinue: () -> Unit
) {
  var selectedCards by remember { mutableStateOf(setOf<String>()) }
  var showCards by remember { mutableStateOf(false) }

  val bonus = cards
    .filter { it.id in selectedCards }
    .sumOf { it.value.toDouble() }
    .toFloat()

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val cardSize = maxWidth * 0.20f

    Row(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(playerName ?: "Игрок")
        Spacer(Modifier.height(12.dp))
        Text("Мод: $playerModifier")
        Text("Бросок: ${playerRoll ?: "-"}")
        Text("Итого: ${playerTotal ?: "-"}")
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(title, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("d$sides")
        Spacer(Modifier.height(8.dp))

        val centerValue = when {
          phase == DiceDuelPhase.PLAYER_ROLL -> playerRoll ?: 1
          phase == DiceDuelPhase.OPPONENT_ROLL -> opponentRoll ?: 1
          else -> (playerRoll ?: opponentRoll ?: 1)
        }

        Image(
          painter = org.jetbrains.compose.resources.painterResource(diceImage(centerValue)),
          contentDescription = null,
          modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(12.dp))

        when (phase) {
          DiceDuelPhase.PLAYER_ROLL,
          DiceDuelPhase.OPPONENT_ROLL -> {
            Button(onClick = onRoll) { Text("Бросить") }
          }

          DiceDuelPhase.PLAYER_MODIFY -> {
            if (canUseCards && !showCards) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onApplyCards(0f, emptyList()) }) {
                  Text("Без карт")
                }
                Button(onClick = {
                  selectedCards = emptySet()
                  showCards = true
                }) {
                  Text("Карты")
                }
              }
            } else if (showCards) {
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(cards, key = { it.id }) { card ->
                  val isSelected = card.id in selectedCards
                  val p = cardPainter(card.image)

                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                      selectedCards = if (isSelected) selectedCards - card.id else selectedCards + card.id
                    }
                  ) {
                    p?.let {
                      Box {
                        Image(
                          painter = it,
                          contentDescription = null,
                          modifier = Modifier.size(cardSize)
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

              Button(onClick = {
                val used = selectedCards.toList()
                onApplyCards(bonus, used)
                selectedCards = emptySet()
                showCards = false
              }) {
                Text(if (selectedCards.isEmpty()) "Применить" else "Применить (+$bonus)")
              }
            } else {
              Button(onClick = onContinue) { Text("Продолжить") }
            }
          }

          DiceDuelPhase.RESOLVE -> {
            Text(resultText ?: "")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onContinue) { Text("Продолжить") }
          }

          DiceDuelPhase.START -> Unit
        }
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(opponentName)
        Spacer(Modifier.height(12.dp))
        opponentImagePainter?.let {
          Image(
            painter = it,
            contentDescription = opponentName,
            modifier = Modifier.size(220.dp)
          )
        }
        Spacer(Modifier.height(8.dp))
        Text("Мод: $opponentModifier")
        Text("Бросок: ${opponentRoll ?: "-"}")
        Text("Итого: ${opponentTotal ?: "-"}")
      }
    }
  }
}