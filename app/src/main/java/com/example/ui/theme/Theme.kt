package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = SystemNeonCyan,
  onPrimary = SystemDarkBlue,
  secondary = SystemNeonBlue,
  onSecondary = TextCrispWhite,
  tertiary = SystemPurple,
  background = SystemDarkBlue,
  onBackground = TextCrispWhite,
  surface = SystemCardDark,
  onSurface = TextCrispWhite,
  outline = DarkGreyBorder
)

private val LightColorScheme = darkColorScheme( // Force dark theme vibe even in light mode to keep Shadow Ascension feel
  primary = SystemNeonCyan,
  onPrimary = SystemDarkBlue,
  secondary = SystemNeonBlue,
  onSecondary = TextCrispWhite,
  tertiary = SystemPurple,
  background = SystemDarkBlue,
  onBackground = TextCrispWhite,
  surface = SystemCardDark,
  onSurface = TextCrispWhite,
  outline = DarkGreyBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark theme for Shadow Ascension visual system
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve customized neon branding
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
