package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olegkos.vnengine.GameLoading.AssetReader
import com.olegkos.vnengine.engine.VisibleCharacter
import com.olegkos.vnengine.engine.asserts.AssetPathResolver

@Composable
fun VisibleCharacterView(
  character: VisibleCharacter,
  isSpeaking: Boolean,
  positionOffset: Dp,
  assets: AssetPathResolver,
  reader: AssetReader,
  modifier: Modifier = Modifier,
) {
  val painter = rememberBitmapPainter(assets.character(character.image), reader) ?: return

  Box(
    modifier = modifier
      .offset(x = positionOffset)
      .graphicsLayer {
        scaleX = character.scale
        scaleY = character.scale
        transformOrigin = TransformOrigin(0.5f, 1f)
      },
  ) {
    Image(
      painter = painter,
      contentDescription = null,
      contentScale = ContentScale.Fit,
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
