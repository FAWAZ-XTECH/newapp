package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val M9999DarkColorScheme = darkColorScheme(
  primary = GoldPrimary,
  onPrimary = ObsidianBlack,
  primaryContainer = ObsidianElevated,
  onPrimaryContainer = GoldChampagne,
  secondary = GoldBright,
  onSecondary = ObsidianBlack,
  secondaryContainer = ColorTokens.SecondaryContainer,
  onSecondaryContainer = GoldChampagne,
  tertiary = EmeraldWin,
  onTertiary = ObsidianBlack,
  background = ObsidianBlack,
  onBackground = TextPrimary,
  surface = ObsidianSurface,
  onSurface = TextPrimary,
  surfaceVariant = ObsidianCard,
  onSurfaceVariant = TextSecondary,
  surfaceContainerLowest = ObsidianBlack,
  surfaceContainerLow = ObsidianSurface,
  surfaceContainer = ObsidianCard,
  surfaceContainerHigh = ObsidianElevated,
  surfaceContainerHighest = ColorTokens.HighestSurface,
  outline = ObsidianBorder,
  outlineVariant = ColorTokens.SubtleBorder,
  error = RubyLoss,
  onError = TextPrimary,
  errorContainer = RubySurface,
  onErrorContainer = RubyLoss,
)

private object ColorTokens {
  val SecondaryContainer = androidx.compose.ui.graphics.Color(0xFF2A2312)
  val HighestSurface = androidx.compose.ui.graphics.Color(0xFF2B2B36)
  val SubtleBorder = androidx.compose.ui.graphics.Color(0xFF24242E)
}

val M9999Shapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun M9999Theme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = M9999DarkColorScheme,
    typography = M9999Typography,
    shapes = M9999Shapes,
    content = content,
  )
}
