package com.echocall.lab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF245D5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8CFCD),
    onPrimaryContainer = Color(0xFF073736),
    secondary = Color(0xFF4C6261),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE8E6),
    onSecondaryContainer = Color(0xFF263B3A),
    tertiary = Color(0xFF4B607C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E3FF),
    onTertiaryContainer = Color(0xFF1C304A),
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFF8FAF9),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDEE4E2),
    onSurfaceVariant = Color(0xFF424846),
    outline = Color(0xFF727977),
    outlineVariant = Color(0xFFC2C9C7),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8ECAC7),
    onPrimary = Color(0xFF003736),
    primaryContainer = Color(0xFF14504F),
    onPrimaryContainer = Color(0xFFA8CFCD),
    secondary = Color(0xFFB3CCCA),
    onSecondary = Color(0xFF1E3534),
    secondaryContainer = Color(0xFF354B4A),
    onSecondaryContainer = Color(0xFFCFE8E6),
    tertiary = Color(0xFFB3C7E9),
    onTertiary = Color(0xFF1D314B),
    tertiaryContainer = Color(0xFF344863),
    onTertiaryContainer = Color(0xFFD6E3FF),
    background = Color(0xFF101414),
    onBackground = Color(0xFFE0E3E2),
    surface = Color(0xFF101414),
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF424846),
    onSurfaceVariant = Color(0xFFC2C9C7),
    outline = Color(0xFF8C9391),
    outlineVariant = Color(0xFF424846),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun EchoCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
