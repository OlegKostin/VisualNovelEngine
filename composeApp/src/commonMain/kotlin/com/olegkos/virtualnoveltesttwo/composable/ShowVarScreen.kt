package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import com.olegkos.virtualnoveltesttwo.theme.SkikoSafeText
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min

private val ShowVarCardBg = Color.Black.copy(alpha = 0.1f)
private val ShowVarTextPrimary = Color.White
private val ShowVarTextSecondary = Color(0xE6FFFFFF)

@Immutable
private data class ShowVarTypography(
  val primary: TextUnit,
  val value: TextUnit,
  val description: TextUnit,
) {
  companion object {
    fun fromViewport(widthDp: Float, heightDp: Float): ShowVarTypography {
      val scale = min(widthDp / 960f, heightDp / 540f).coerceIn(0.85f, 1.5f)
      fun sz(px: Float) = (px * scale).sp
      return ShowVarTypography(
        primary = sz(18f),
        value = sz(22f),
        description = sz(13f),
      )
    }
  }
}

@Composable
fun ShowVarScreen(
  name: String,
  value: String,
  description: String,
  onNext: () -> Unit,
) {
  val stat = StatType.fromKey(name)
  val titleText = stat?.title ?: name
  val interactionSource = remember { MutableInteractionSource() }
  val cardShape = remember { RoundedCornerShape(16.dp) }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = interactionSource,
        indication = null,
      ) { onNext() },
    contentAlignment = Alignment.Center,
  ) {
    val typography = remember(maxWidth, maxHeight) {
      ShowVarTypography.fromViewport(maxWidth.value, maxHeight.value)
    }
    val cardHeight = maxHeight * 0.2f
    val cardWidth = maxWidth * 0.60f
    val imageHeight = min(maxHeight * 0.18f, cardWidth * 0.35f)

    Column(
      modifier = Modifier.width(cardWidth),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      stat?.let {
        Image(
          painter = painterResource(it.image),
          contentDescription = null,
          modifier = Modifier.height(imageHeight),
        )
        Spacer(Modifier.height(cardHeight * 0.12f))
      }

      ShowVarTextCard(
        title = titleText,
        value = value,
        description = description,
        typography = typography,
        shape = cardShape,
        modifier = Modifier
          .fillMaxWidth()
          .height(cardHeight),
      )
    }
  }
}

@Composable
private fun ShowVarTextCard(
  title: String,
  value: String,
  description: String,
  typography: ShowVarTypography,
  shape: RoundedCornerShape,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(shape)
      .background(ShowVarCardBg)
      .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        Modifier.weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        SkikoSafeText(
          text = title,
          fontSize = typography.primary,
          fontWeight = FontWeight.SemiBold,
          color = ShowVarTextPrimary,
          maxLines = 2,
        )
      }
      Box(
        Modifier.weight(1f),
        contentAlignment = Alignment.Center,
      ) {
        SkikoSafeText(
          text = value,
          fontSize = typography.value,
          fontWeight = FontWeight.Bold,
          color = ShowVarTextPrimary,
          maxLines = 2,
        )
      }
    }

    Box(
      Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      SkikoSafeText(
        text = description,
        fontSize = typography.description,
        color = ShowVarTextSecondary,
        maxLines = 3,
      )
    }
  }
}
