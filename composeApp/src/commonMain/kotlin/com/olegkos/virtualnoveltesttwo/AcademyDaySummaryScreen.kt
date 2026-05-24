package com.olegkos.virtualnoveltesttwo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnoveltesttwo.theme.AcademyHubTypography
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import com.olegkos.virtualnoveltesttwo.theme.VnButtonSurface
import com.olegkos.virtualnoveltesttwo.theme.VnOutlinedButton
import com.olegkos.vnengine.engine.EngineOutput

private val Accent = Color(0xFFBBDEFB)
private val BaseBg = Color(0xFF10141C)
private val PanelBg = Color(0xFF1C2838)
private val Positive = Color(0xFF81C784)
private val Negative = Color(0xFFE57373)

@Composable
fun AcademyDaySummaryScreen(
  output: EngineOutput.ShowAcademyDaySummary,
  onContinue: () -> Unit,
) {
  BoxWithConstraints(
    Modifier
      .fillMaxSize()
      .background(BaseBg),
  ) {
    val typography = remember(maxHeight, maxWidth) {
      AcademyHubTypography.fromViewport(maxHeight.value, maxWidth.value)
    }
    Column(
      Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Column(
        Modifier
          .fillMaxWidth()
          .background(PanelBg, RoundedCornerShape(12.dp))
          .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
          .padding(20.dp),
      ) {
        SkikoSafeText(
          text = "${output.title} ${output.day}",
          fontSize = typography.dayHeader,
          color = Color.White,
          modifier = Modifier.padding(bottom = 16.dp),
        )

        SkikoSafeText(
          text = "Изменения за день",
          fontSize = typography.section,
          color = Accent,
          modifier = Modifier.padding(bottom = 10.dp),
        )

        output.changes.forEach { change ->
          VarChangeRow(change, typography)
          Spacer(Modifier.height(8.dp))
        }

        VnOutlinedButton(
          onClick = onContinue,
          surface = VnButtonSurface.Dark,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        ) {
          Text("Продолжить", fontSize = typography.button, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun VarChangeRow(
  change: EngineOutput.AcademyDayVarChangeUi,
  typography: AcademyHubTypography,
) {
  val deltaColor = when (change.deltaSign) {
    1 -> Positive
    -1 -> Negative
    else -> Color(0xBBFFFFFF)
  }
  val deltaText = change.delta

  Column(
    Modifier
      .fillMaxWidth()
      .background(Color(0xFF243044), RoundedCornerShape(8.dp))
      .padding(horizontal = 12.dp, vertical = 10.dp),
  ) {
    Text(change.label, fontSize = typography.stat, color = Color.White)
    Row(
      Modifier
        .fillMaxWidth()
        .padding(top = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${change.before} → ${change.after}",
        fontSize = typography.hint,
        color = Color(0xCCFFFFFF),
      )
      Text(
        text = deltaText,
        fontSize = typography.body,
        color = deltaColor,
      )
    }
  }
}
