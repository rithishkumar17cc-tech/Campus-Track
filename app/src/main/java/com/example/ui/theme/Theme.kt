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

private val ElegantColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = ElegantSecondary,
    tertiary = ElegantTertiary,
    background = DarkBg,
    surface = DarkSurface,
    onBackground = DarkTextBody,
    onSurface = DarkTextBody,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    error = ElegantError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Elegant Dark aesthetics
    dynamicColor: Boolean = false, // Disable dynamic colors to keep design consistency
    content: @Composable () -> Unit,
) {
    val colorScheme = ElegantColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
