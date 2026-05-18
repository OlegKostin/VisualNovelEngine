package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.EngineOutput

private val Accent = Color(0xFFBBDEFB)
private val BaseBg = Color(0xFF10141C)
private val Divider = Color(0x33FFFFFF)

@Composable
fun AcademyHubScreen(
  output: EngineOutput.ShowAcademyHub,
  viewModel: GameViewModel,
) {
  val stats = viewModel.playerStatsUi()

  Column(Modifier.fillMaxSize().background(BaseBg)) {
    SkikoSafeText(
      text = "День ${output.day}",
      fontSize = 18.sp,
      color = Color.White,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
    )

    Row(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      output.timeSlots.forEach { slot ->
        PhaseColumn(
          slot = slot,
          modifier = Modifier.weight(1f),
          onSelectActivity = { viewModel.academySetActivity(slot.phaseId, it) },
        )
      }

      StatsAndBuildingsColumn(
        modifier = Modifier.weight(1f),
        output = output,
        stats = stats,
        onSelectBuilding = { id, selected ->
          if (selected) viewModel.academySelectBuilding(null)
          else viewModel.academySelectBuilding(id)
        },
        onCommitDay = { viewModel.academyCommitDay() },
      )
    }
  }
}

@Composable
private fun PhaseColumn(
  slot: EngineOutput.AcademyTimeSlotUi,
  modifier: Modifier = Modifier,
  onSelectActivity: (String) -> Unit,
) {
  val hasSelection = slot.selectedActivityId != null
  val zoneColor = phaseZoneColor(slot.phaseId)
  val borderColor = if (hasSelection) Accent else Divider

  Column(
    modifier
      .fillMaxHeight()
      .background(zoneColor, RoundedCornerShape(10.dp))
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SkikoSafeText(
      text = slot.label,
      fontSize = 15.sp,
      color = Color.White,
      modifier = Modifier.padding(bottom = 10.dp),
    )

    if (slot.activities.isEmpty()) {
      SkikoSafeText("Нет действий", fontSize = 11.sp, color = Color(0x99FFFFFF))
    } else {
      slot.activities.forEach { act ->
        val selected = act.id == slot.selectedActivityId
        VnOutlinedButton(
          onClick = { onSelectActivity(act.id) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        ) {
          Text(
            text = act.label,
            fontSize = 12.sp,
            color = if (selected) Accent else Color.White,
          )
        }
      }
    }
  }
}

@Composable
private fun StatsAndBuildingsColumn(
  output: EngineOutput.ShowAcademyHub,
  stats: com.olegkos.virtualnovelapp.PlayerStatsUi,
  modifier: Modifier = Modifier,
  onSelectBuilding: (buildingId: String, currentlySelected: Boolean) -> Unit,
  onCommitDay: () -> Unit,
) {
  Column(
    modifier
      .fillMaxHeight()
      .background(Color(0xFF1C2838), RoundedCornerShape(10.dp))
      .border(1.dp, Divider, RoundedCornerShape(10.dp))
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    SkikoSafeText("Характеристики", fontSize = 15.sp, color = Accent, modifier = Modifier.padding(bottom = 6.dp))
    StatLine("Здоровье", stats.health.toString())
    StatLine("Рассудок", stats.mentalHealth.toString())
    StatLine("Сила", stats.optStr)
    StatLine("Мудрость", stats.optWisdom)
    StatLine("Воля", stats.optWill)
    StatLine("Удача", stats.optLuck)

    Spacer(Modifier.height(12.dp))
    SkikoSafeText("Строительство (1×/день)", fontSize = 13.sp, color = Accent)

    output.buildingGroups.forEach { group ->
      SkikoSafeText(
        group.label,
        fontSize = 11.sp,
        color = Color(0xBBFFFFFF),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
      )
      group.buildings.forEach { building ->
        VnOutlinedButton(
          onClick = {
            if (building.enabled) {
              onSelectBuilding(building.id, building.selected)
            }
          },
          enabled = building.enabled,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        ) {
          Column(Modifier.fillMaxWidth()) {
            Text(
              text = "${building.label} (ур. ${building.level})",
              fontSize = 11.sp,
              color = if (building.selected) Accent else Color.White,
            )
            building.lockedReason?.let {
              Text(it, fontSize = 9.sp, color = Color(0x99FFFFFF))
            }
          }
        }
      }
    }

    output.commitBlockedReason?.let {
      SkikoSafeText(it, fontSize = 11.sp, color = Color(0xFFFFAB91), modifier = Modifier.padding(top = 8.dp))
    }

    VnOutlinedButton(
      onClick = onCommitDay,
      enabled = output.canCommit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp),
    ) {
      Text("Подтвердить день", fontSize = 13.sp)
    }
  }
}

@Composable
private fun StatLine(label: String, value: String) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(label, fontSize = 11.sp, color = Color(0xCCFFFFFF))
    Text(value, fontSize = 11.sp, color = Color.White)
  }
}

private fun phaseZoneColor(phaseId: String): Color = when (phaseId) {
  "morning" -> Color(0xFF243044)
  "day" -> Color(0xFF2A3A4F)
  "evening" -> Color(0xFF352838)
  "night" -> Color(0xFF1A2230)
  else -> Color(0xFF1E2836)
}
