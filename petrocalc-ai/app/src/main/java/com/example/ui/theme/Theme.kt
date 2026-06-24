package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OilfieldColorScheme = darkColorScheme(
    primary = SafetyOrange,
    onPrimary = Color.Black,
    secondary = PetroleumBlue,
    onSecondary = Color.White,
    background = DarkCharcoal,
    onBackground = TextPrimary,
    surface = DarkCardBg,
    onSurface = TextPrimary,
    tertiary = AlertYellow,
    onTertiary = Color.Black,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Force industrial high-contrast dark theme for safety & sun glare
    MaterialTheme(
        colorScheme = OilfieldColorScheme,
        typography = Typography,
        content = content
    )
}
