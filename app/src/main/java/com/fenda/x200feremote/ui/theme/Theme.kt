package com.fenda.x200feremote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FendaDarkColors = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF041E2B),
    primaryContainer = Color(0xFF0F3549),
    onPrimaryContainer = Color(0xFFD1F0FF),
    secondary = Color(0xFFA855F7),
    onSecondary = Color(0xFF1F0638),
    secondaryContainer = Color(0xFF331452),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFFFB7185),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF151923),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E2433),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

@Composable
fun FendaRemoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FendaDarkColors,
        content = content
    )
}
