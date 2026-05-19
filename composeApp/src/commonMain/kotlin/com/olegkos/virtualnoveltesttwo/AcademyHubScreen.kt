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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

      ResourcesAndBuildColumn(
        modifier = Modifier.weight(1f),
        output = output,
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
        val buildingAct = act.fromBuilding
        VnOutlinedButton(
          onClick = { onSelectActivity(act.id) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
              if (buildingAct) {
                Modifier.border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(8.dp))
              } else {
                Modifier
              }
            ),
        ) {
          Text(
            text = act.label,
            fontSize = 12.sp,
            color = when {
              selected -> Accent
              buildingAct -> Color(0xFFC8E6C9)
              else -> Color.White
            },
          )
        }
      }
    }
  }
}

@Composable
private fun ResourcesAndBuildColumn(
  output: EngineOutput.ShowAcademyHub,
  modifier: Modifier = Modifier,
  onSelectBuilding: (buildingId: String, currentlySelected: Boolean) -> Unit,
  onCommitDay: () -> Unit,
) {
  var buildMenuOpen by remember { mutableStateOf(false) }

  Column(
    modifier
      .fillMaxHeight()
      .background(Color(0xFF1C2838), RoundedCornerShape(10.dp))
      .border(1.dp, Divider, RoundedCornerShape(10.dp))
      .padding(8.dp)
      .verticalScroll(rememberScrollState()),
  ) {
    SkikoSafeText("Ресурсы", fontSize = 15.sp, color = Accent, modifier = Modifier.padding(bottom = 6.dp))
    StatLine("В наличии", output.resources.toString())

    output.selectedBuildingId?.let { selectedId ->
      val label = output.buildingGroups
        .flatMap { it.buildings }
        .firstOrNull { it.id == selectedId }
        ?.label
      if (label != null) {
        SkikoSafeText(
          text = "На стройку: $label",
          fontSize = 11.sp,
          color = Color(0xFFC8E6C9),
          modifier = Modifier.padding(top = 6.dp),
        )
      }
    }

    Spacer(Modifier.height(12.dp))

    VnOutlinedButton(
      onClick = { buildMenuOpen = !buildMenuOpen },
      enabled = output.planning,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = if (buildMenuOpen) "Скрыть постройки" else "Построить",
        fontSize = 13.sp,
      )
    }

    if (buildMenuOpen) {
      Spacer(Modifier.height(8.dp))
      SkikoSafeText(
        text = "Строительство (1× в день)",
        fontSize = 11.sp,
        color = Color(0xBBFFFFFF),
        modifier = Modifier.padding(bottom = 4.dp),
      )

      output.buildingGroups.forEach { group ->
        SkikoSafeText(
          group.label,
          fontSize = 11.sp,
          color = Color(0x99FFFFFF),
          modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
        )
        group.buildings.forEach { building ->
          if (building.isBuilt && !building.enabled) {
            BuiltBuildingRow(building)
          } else {
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
              BuildingRowContent(building)
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
private fun BuiltBuildingRow(building: EngineOutput.AcademyBuildingUi) {
  Column(
    Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .background(Color(0xFF2E4A3A), RoundedCornerShape(8.dp))
      .border(1.dp, Color(0xFF66BB6A), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
  ) {
    BuildingRowContent(building, builtHighlight = true)
  }
}

@Composable
private fun BuildingRowContent(
  building: EngineOutput.AcademyBuildingUi,
  builtHighlight: Boolean = false,
) {
  Column(Modifier.fillMaxWidth()) {
    Text(
      text = building.label,
      fontSize = 12.sp,
      color = when {
        building.selected -> Accent
        builtHighlight || building.isBuilt -> Color(0xFFC8E6C9)
        else -> Color.White
      },
    )
    Text(
      text = buildString {
        append(building.statusLabel)
        building.buildCost?.let { append(" · $it рес.") }
      },
      fontSize = 10.sp,
      color = Color(0xBBFFFFFF),
    )
    building.lockedReason?.let {
      Text(it, fontSize = 9.sp, color = Color(0x99FFFFFF))
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
