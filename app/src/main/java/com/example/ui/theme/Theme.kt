package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
  SYSTEM, LIGHT, DARK
}

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color(0xFF022B1E),
    primaryContainer = DarkGreenPrimaryContainer,
    onPrimaryContainer = MintGreenAccent,
    secondary = MintGreenAccent,
    onSecondary = Color(0xFF022B1E),
    tertiary = GoldAccent,
    background = DarkGreenBackground,
    surface = DarkGreenSurface,
    surfaceVariant = DarkGreenCard,
    outline = DarkGreenBorder,
    outlineVariant = Color(0xFF1E3A2F),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = LossRed,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    tertiary = WarningAmberDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFE2E8F0),
    outline = CardBorderLight,
    outlineVariant = OutlineLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = LossRed,
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK,
  dynamicColor: Boolean = false, // Use our tailored Emerald palette by default for brand consistency
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val darkTheme = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemDark
    else -> true
  }

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

