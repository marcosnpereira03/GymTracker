package org.marcosnpereira03.gymtracker.presentation.navigation

/**
 * Rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout?workoutId={workoutId}") {
        fun createRoute(workoutId: String? = null): String {
            return if (workoutId != null) "workout?workoutId=$workoutId" else "workout"
        }
    }
    object History : Screen("history")
    object Exercises : Screen("exercises")
    object Profile : Screen("profile")
}
