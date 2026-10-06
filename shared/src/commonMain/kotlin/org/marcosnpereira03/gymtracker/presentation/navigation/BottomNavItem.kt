package org.marcosnpereira03.gymtracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Elementos de la barra de navegación inferior.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Hoy", Icons.Default.Home)
    object Workout : BottomNavItem(Screen.Workout.createRoute(), "Entrenar", Icons.Default.PlayArrow)
    object History : BottomNavItem(Screen.History.route, "Historial", Icons.Default.DateRange)
    object Exercises : BottomNavItem(Screen.Exercises.route, "Ejercicios", Icons.Default.List)
    object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.History,
    BottomNavItem.Workout,
    BottomNavItem.Exercises,
    BottomNavItem.Profile
)
