package org.marcosnpereira03.gymtracker.presentation.navigation

/**
 * Rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout?workoutId={workoutId}&date={date}") {
        fun createRoute(workoutId: String? = null, date: String? = null): String {
            val params = mutableListOf<String>()
            if (!workoutId.isNullOrBlank()) params.add("workoutId=$workoutId")
            if (!date.isNullOrBlank()) params.add("date=$date")
            return if (params.isNotEmpty()) "workout?${params.joinToString("&")}" else "workout"
        }
    }
    object History : Screen("history")
    object Exercises : Screen("exercises")
    object Profile : Screen("profile")
    object Auth : Screen("auth")
}
