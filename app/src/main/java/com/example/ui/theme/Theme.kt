package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CosmicDarkPrimary,
    secondary = CosmicDarkSecondary,
    tertiary = CosmicDarkTertiary,
    background = CosmicDarkBgStart,
    surface = CosmicDarkSurface,
    onPrimary = CosmicDarkBgStart,
    onSecondary = CosmicDarkBgStart,
    onTertiary = CosmicDarkBgStart,
    onBackground = CosmicDarkText,
    onSurface = CosmicDarkText
)

private val LightColorScheme = lightColorScheme(
    primary = TwilightLightPrimary,
    secondary = TwilightLightSecondary,
    tertiary = TwilightLightTertiary,
    background = TwilightLightBgStart,
    surface = TwilightLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TwilightLightText,
    onSurface = TwilightLightText
)

@Composable
fun PocketCalTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
