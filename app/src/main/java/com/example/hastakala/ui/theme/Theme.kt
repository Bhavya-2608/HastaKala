package com.example.hastakala.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ArtSage,
    secondary = ArtOchre,
    tertiary = ArtTerracotta,
    background = ArtInk,
    surface = ArtCharcoal,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = ArtPaper,
    onSurface = ArtPaper
)

private val LightColorScheme = lightColorScheme(
    primary = ArtCharcoal,
    secondary = ArtDeepOlive,
    tertiary = ArtTerracotta,
    background = ArtPaper,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ArtCharcoal,
    onSurface = ArtCharcoal,
    secondaryContainer = ArtWarmSand,
    onSecondaryContainer = ArtCharcoal
)

@Composable
fun HastaKalaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
