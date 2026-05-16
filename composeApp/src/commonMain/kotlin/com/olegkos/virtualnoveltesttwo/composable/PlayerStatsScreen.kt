package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.IconStatLayout
import com.olegkos.virtualnoveltesttwo.IconStatRow
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton

private val coreOptKeys = listOf("opt_str", "opt_wisdom", "opt_will", "opt_luck")
private val statValueColor = Color(0xFFE8ECF5)
private val statLabelColor = Color(0xFFB8C0D0)

@Composable
fun PlayerStatsScreen(
  viewModel: GameViewModel,
  refreshKey: Any?,
  onDismiss: () -> Unit
) {
  val stats = remember(refreshKey) { viewModel.playerStatsUi() }
  val scroll = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xE6000000))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        VnOutlinedButton(onClick = onDismiss) {
          Text("Закрыть")
        }
        Text(
          text = "Характеристики",
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
          modifier = Modifier.align(Alignment.Center)
        )
      }

      Spacer(Modifier.height(12.dp))

      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(scroll)
      ) {
        val statIconSize = maxWidth * 0.09f
        val rowFontSize = (maxHeight.value * 0.04f).coerceIn(14f, 22f).sp

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = StatType.HP.title,
                  color = Color(0xFFB8C0D0),
                  style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                PulsingHoverBox(
                  statKey = "health",
                  enablePulse = true,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  IconStatRow(
                    stat = StatType.HP,
                    count = stats.health,
                    fontSize = rowFontSize,
                    layout = IconStatLayout.CenterCluster
                  )
                }
              }
            }

            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = StatType.MENTAL.title,
                  color = Color(0xFFB8C0D0),
                  style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                PulsingHoverBox(
                  statKey = "mental_health",
                  enablePulse = true,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  IconStatRow(
                    stat = StatType.MENTAL,
                    count = stats.mentalHealth,
                    fontSize = rowFontSize,
                    layout = IconStatLayout.CenterCluster
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val optValues = listOf(
              stats.optStr,
              stats.optWisdom,
              stats.optWill,
              stats.optLuck
            )
            coreOptKeys.forEachIndexed { index, key ->
              PulsingStatValue(
                statKey = key,
                displayValue = optValues[index],
                statIconSize = statIconSize,
                enablePulse = true,
                modifier = Modifier.weight(1f)
              )
            }
          }

          stats.extraOptVars.forEach { (label, value) ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = label,
                color = statLabelColor,
                style = MaterialTheme.typography.bodyLarge
              )
              Text(
                text = value,
                color = statValueColor,
                style = MaterialTheme.typography.headlineSmall
              )
            }
          }
        }
      }
    }
  }
}
