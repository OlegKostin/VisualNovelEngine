package com.olegkos.virtualnoveltesttwo

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import virtualnoveltesttwo.composeapp.generated.resources.Res
import virtualnoveltesttwo.composeapp.generated.resources.d1
import virtualnoveltesttwo.composeapp.generated.resources.d10
import virtualnoveltesttwo.composeapp.generated.resources.d11
import virtualnoveltesttwo.composeapp.generated.resources.d12
import virtualnoveltesttwo.composeapp.generated.resources.d13
import virtualnoveltesttwo.composeapp.generated.resources.d14
import virtualnoveltesttwo.composeapp.generated.resources.d15
import virtualnoveltesttwo.composeapp.generated.resources.d16
import virtualnoveltesttwo.composeapp.generated.resources.d17
import virtualnoveltesttwo.composeapp.generated.resources.d18
import virtualnoveltesttwo.composeapp.generated.resources.d19
import virtualnoveltesttwo.composeapp.generated.resources.d2
import virtualnoveltesttwo.composeapp.generated.resources.d20
import virtualnoveltesttwo.composeapp.generated.resources.d3
import virtualnoveltesttwo.composeapp.generated.resources.d4
import virtualnoveltesttwo.composeapp.generated.resources.d5
import virtualnoveltesttwo.composeapp.generated.resources.d6
import virtualnoveltesttwo.composeapp.generated.resources.d7
import virtualnoveltesttwo.composeapp.generated.resources.d8
import virtualnoveltesttwo.composeapp.generated.resources.d9

@Composable
fun diceFacePainter(face: Int): Painter =
  painterResource(diceImage(face.coerceIn(1, 20)))

fun diceImage(value: Int): DrawableResource =
  when (value) {
    1 -> Res.drawable.d1
    2 -> Res.drawable.d2
    3 -> Res.drawable.d3
    4 -> Res.drawable.d4
    5 -> Res.drawable.d5
    6 -> Res.drawable.d6
    7 -> Res.drawable.d7
    8 -> Res.drawable.d8
    9 -> Res.drawable.d9
    10 -> Res.drawable.d10
    11 -> Res.drawable.d11
    12 -> Res.drawable.d12
    13 -> Res.drawable.d13
    14 -> Res.drawable.d14
    15 -> Res.drawable.d15
    16 -> Res.drawable.d16
    17 -> Res.drawable.d17
    18 -> Res.drawable.d18
    19 -> Res.drawable.d19
    else -> Res.drawable.d20
  }
