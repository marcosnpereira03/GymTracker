package org.marcosnpereira03.gymtracker.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores oficial Tailwind CSS (Zinc + Emerald) para GymTracker.
 */

// Tailwind Zinc (Dark Mode Backgrounds, Surfaces & Borders)
val Zinc950 = Color(0xFF09090B) // Fondo principal de la app
val Zinc900 = Color(0xFF18181B) // Superficies de tarjetas y barras de navegación
val Zinc800 = Color(0xFF27272A) // Contenedores de inputs y fondos secundarios
val Zinc700 = Color(0xFF3F3F46) // Bordes y divisores
val Zinc600 = Color(0xFF52525B) // Bordes interactivos
val Zinc500 = Color(0xFF71717A) // Texto muted / placeholders
val Zinc400 = Color(0xFFA1A1AA) // Texto secundario (zinc-400)
val Zinc300 = Color(0xFFD4D4D8) // Texto intermedio
val Zinc100 = Color(0xFFF4F4F5) // Texto de alto contraste
val White = Color(0xFFFFFFFF)   // Blanco puro

// Tailwind Emerald (Accents, Highlights & Gradients)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399) // Acento brillante (emerald-400)
val Emerald500 = Color(0xFF10B981) // Acento de acción principal
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857) // Fin de degradado hero card
val Emerald800 = Color(0xFF065F46)
val Emerald900 = Color(0xFF064E3B) // Inicio de degradado hero card
val Emerald950 = Color(0xFF022C22) // Fondos con tinte esmeralda muy sutil

// Colores de estado
val Red500 = Color(0xFFEF4444)
val Cyan400 = Color(0xFF22D3EE)

// Alias de conveniencia para Compose Theme
val DarkBackground = Zinc950
val DarkSurface = Zinc900
val DarkSurfaceCard = Zinc900
val DarkSurfaceBorder = Zinc800
val EmeraldPrimary = Emerald400
val TextPrimary = White
val TextSecondary = Zinc400
val TextMuted = Zinc500
val TextOnEmerald = Color(0xFF000000)
val AccentRed = Red500
val AccentCyan = Cyan400
val EmeraldGradientStart = Emerald900
val EmeraldGradientEnd = Emerald700
