package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AuraCyan,
    onPrimary = Color.Black,
    primaryContainer = AuraDarkSurfaceVariant,
    onPrimaryContainer = AuraCyanGlow,
    secondary = AuraViolet,
    onSecondary = Color.White,
    secondaryContainer = AuraDarkSurfaceCard,
    onSecondaryContainer = AuraVioletLight,
    tertiary = AuraElectricBlue,
    onTertiary = Color.White,
    background = AuraDarkBackground,
    onBackground = TextPrimary,
    surface = AuraDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = AuraDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AuraError,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // AURA is intentionally designed with a cinematic dark anime aesthetic
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
