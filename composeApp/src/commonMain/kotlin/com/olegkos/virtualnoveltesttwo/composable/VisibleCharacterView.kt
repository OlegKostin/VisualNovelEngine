package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.VisibleCharacter
import com.olegkos.vnengine.engine.asserts.AssetPathResolver

private const val CharacterBaseHeightFraction = 0.95f

@Composable
fun VisibleCharacterView(
  character: VisibleCharacter,
  isSpeaking: Boolean,
  positionOffset: Dp,
  screenHeight: Dp,
  assets: AssetPathResolver,
  reader: AssetReader,
  modifier: Modifier = Modifier,
) {
  val painter = rememberBitmapPainter(assets.character(character.image), reader) ?: return
  val drawHeight = screenHeight * CharacterBaseHeightFraction * character.scale.coerceAtLeast(0.01f)

  Box(
    modifier = modifier
      .offset(x = positionOffset)
      .height(drawHeight),
  ) {
    Image(
      painter = painter,
      contentDescription = null,
      modifier = Modifier.fillMaxHeight(),
      contentScale = ContentScale.FillHeight,
    )
    if (isSpeaking) {
      Box(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 16.dp)
          .size(12.dp)
          .background(Color(0xFFE53935), CircleShape),
      )
    }
  }
}
