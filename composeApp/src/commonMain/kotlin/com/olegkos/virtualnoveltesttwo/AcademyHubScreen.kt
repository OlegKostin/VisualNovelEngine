package com.olegkos.virtualnoveltesttwo

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnovelapp.GameViewModel
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.EngineOutput

private val Panel = Color(0xE6182438)
private val Accent = Color(0xFFBBDEFB)

@Composable
fun AcademyHubScreen(
  output: EngineOutput.ShowAcademyHub,
  viewModel: GameViewModel,
  backgroundPainter: @Composable (String) -> androidx.compose.ui.graphics.painter.Painter?,
) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val mapWidth = maxWidth
    val mapHeight = maxHeight

    backgroundPainter(output.background)?.let { painter ->
      androidx.compose.foundation.Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
    }

    Box(Modifier.fillMaxSize()) {
      output.buildingGroups.flatMap { it.buildings }.forEach { building ->
        val x = mapWidth * (building.xPercent / 100f)
        val y = mapHeight * (building.yPercent / 100f)
        Box(
          Modifier
            .offset(x = x, y = y)
            .background(
              when {
                building.selected -> Color(0x99BBDEFB)
                building.enabled -> Color(0x664CAF50)
                else -> Color(0x66444444)
              },
              RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(building.label, fontSize = 11.sp, color = Color.White)
        }
      }
    }

    Column(
      Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(Panel, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      SkikoSafeText(
        "День ${output.day}",
        fontSize = 22.sp,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp),
      )

      SkikoSafeText("Строительство (1 раз в день)", fontSize = 14.sp, color = Accent)
      output.buildingGroups.forEach { group ->
        SkikoSafeText(group.label, fontSize = 13.sp, color = Color(0xCCFFFFFF), modifier = Modifier.padding(top = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          group.buildings.forEach { building ->
            VnOutlinedButton(
              onClick = {
                if (building.enabled) {
                  viewModel.academySelectBuilding(
                    if (building.selected) null else building.id
                  )
                }
              },
              enabled = building.enabled,
              modifier = Modifier.weight(1f),
            ) {
              Column {
                Text(building.label, fontSize = 12.sp)
                Text("ур. ${building.level}", fontSize = 10.sp)
                building.lockedReason?.let {
                  Text(it, fontSize = 9.sp, color = Color(0x99FFFFFF))
                }
              }
            }
          }
        }
      }

      Spacer(Modifier.height(12.dp))
      SkikoSafeText("План дня", fontSize = 14.sp, color = Accent)
      output.timeSlots.forEach { slot ->
        ActivitySlotDropdown(
          slot = slot,
          onSelect = { viewModel.academySetActivity(slot.phaseId, it) }
        )
      }

      output.commitBlockedReason?.let {
        SkikoSafeText(it, fontSize = 12.sp, color = Color(0xFFFFAB91), modifier = Modifier.padding(top = 8.dp))
      }

      VnOutlinedButton(
        onClick = { viewModel.academyCommitDay() },
        enabled = output.canCommit,
        modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
      ) {
        Text("Подтвердить день")
      }
    }
  }
}

@Composable
private fun ActivitySlotDropdown(
  slot: EngineOutput.AcademyTimeSlotUi,
  onSelect: (String) -> Unit,
) {
  Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
    SkikoSafeText(slot.label, fontSize = 13.sp, color = Color.White)
    Row(
      Modifier.fillMaxWidth().padding(top = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      slot.activities.forEach { act ->
        val selected = act.id == slot.selectedActivityId
        VnOutlinedButton(onClick = { onSelect(act.id) }) {
          Text(
            act.label,
            fontSize = 11.sp,
            color = if (selected) Accent else Color.White,
          )
        }
      }
    }
  }
}
