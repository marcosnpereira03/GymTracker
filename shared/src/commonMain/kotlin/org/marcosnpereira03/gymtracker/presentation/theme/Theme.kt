package org.marcosnpereira03.gymtracker.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GymTrackerDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = TextOnEmerald,
    primaryContainer = DarkSurfaceCard,
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = TextOnEmerald,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    error = AccentRed,
    onError = TextPrimary
)

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = true, // Por defecto diseño dark deportivo
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GymTrackerDarkColorScheme,
        content = content
    )
}
