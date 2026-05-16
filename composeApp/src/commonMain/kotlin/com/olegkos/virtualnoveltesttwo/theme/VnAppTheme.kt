package com.olegkos.virtualnoveltesttwo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val ink = Color(0xFF121820)
private val inkMuted = Color(0xFF2A3344)
private val slateLine = Color(0xFF7C8AA8)
private val slateMuted = Color(0xFF5A6B86)
private val accentBlue = Color(0xFF3D5A80)

private val VnColorScheme = lightColorScheme(
  primary = accentBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD4E0F2),
  onPrimaryContainer = ink,
  secondary = slateMuted,
  onSecondary = Color.White,
  tertiary = Color(0xFF4A6678),
  onTertiary = Color.White,
  background = Color(0xFFF0F2F8),
  onBackground = ink,
  surface = Color(0xFFFAFBFD),
  onSurface = ink,
  surfaceVariant = Color(0xFFE2E8F2),
  onSurfaceVariant = inkMuted,
  outline = slateLine,
  outlineVariant = Color(0xFFC8D0E0),
  error = Color(0xFFB3261E),
  onError = Color.White
)

/** Material3 по умолчанию задаёт lineHeightStyle → на Skiko Desktop падает setBaselineShift. */
private fun TextStyle.skikoSafe(): TextStyle =
  copy(lineHeightStyle = null)

private fun Typography.skikoSafe(): Typography {
  val base = Typography()
  return Typography(
    displayLarge = base.displayLarge.skikoSafe(),
    displayMedium = base.displayMedium.skikoSafe(),
    displaySmall = base.displaySmall.skikoSafe(),
    headlineLarge = base.headlineLarge.skikoSafe(),
    headlineMedium = base.headlineMedium.skikoSafe(),
    headlineSmall = base.headlineSmall.skikoSafe(),
    titleLarge = base.titleLarge.skikoSafe(),
    titleMedium = base.titleMedium.skikoSafe(),
    titleSmall = base.titleSmall.skikoSafe(),
    bodyLarge = base.bodyLarge.skikoSafe(),
    bodyMedium = base.bodyMedium.skikoSafe(),
    bodySmall = base.bodySmall.skikoSafe(),
    labelLarge = base.labelLarge.skikoSafe(),
    labelMedium = base.labelMedium.skikoSafe(),
    labelSmall = base.labelSmall.skikoSafe()
  )
}

private val VnTypography = Typography().skikoSafe()

@Composable
fun VnAppTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = VnColorScheme,
    typography = VnTypography,
    content = content
  )
}
