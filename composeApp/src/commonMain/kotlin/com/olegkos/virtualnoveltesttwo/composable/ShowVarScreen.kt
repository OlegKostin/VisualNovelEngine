package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import org.jetbrains.compose.resources.painterResource

@Composable
fun ShowVarScreen(
  name: String,
  value: String,
  text: String,
  onNext: () -> Unit
) {
  val stat = StatType.fromKey(name)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .clickable { onNext() },
    contentAlignment = Alignment.Center
  ) {

    val imageHeight = maxHeight * 0.2f

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      stat?.let {
        Image(
          painter = painterResource(it.image),
          contentDescription = null,
          modifier = Modifier
            .height(imageHeight)
            .aspectRatio(1f)
        )
      }

      Spacer(Modifier.height(16.dp))

      Text(
        text = text,
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text = "+$value"
      )
    }
  }
}