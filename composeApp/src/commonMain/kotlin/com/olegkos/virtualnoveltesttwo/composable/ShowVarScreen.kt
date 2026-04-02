package com.olegkos.virtualnoveltesttwo.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olegkos.virtualnoveltesttwo.mappers.StatType
import org.jetbrains.compose.resources.painterResource

@Composable
fun ShowVarScreen(
  name: String,
  value: String,
  description: String,
  onNext: () -> Unit
) {
  val stat = StatType.fromKey(name)
  val interactionSource = remember { MutableInteractionSource() }
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onNext() },
    contentAlignment = Alignment.Center
  ) {

    val imageHeight = maxHeight * 0.25f

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      stat?.let {
        Image(
          painter = painterResource(it.image),
          contentDescription = null,
          modifier = Modifier.height(imageHeight)
        )
      }

      Spacer(Modifier.height(16.dp))

      Text(
        text = stat?.title ?: name
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text = value
      )

      Spacer(Modifier.height(12.dp))
      Text(
        text = description
      )

    }
  }
}