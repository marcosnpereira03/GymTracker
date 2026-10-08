package org.marcosnpereira03.gymtracker.presentation.home

import org.marcosnpereira03.gymtracker.domain.model.AuthUser
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Representa un entrenamiento en el dashboard de inicio con su desglose de series por ejercicio.
 */
data class HomeWorkoutItem(
    val workout: Workout,
    val exercisesSummary: List<Pair<Int, String>> = emptyList()
)

/**
 * Estado UI inmutable para la pantalla principal (Hoy).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val latestWorkout: Workout? = null,
    val latestWeight: BodyWeightLog? = null,
    val recentWorkouts: List<HomeWorkoutItem> = emptyList(),
    val totalVolumeLatestWorkout: Double = 0.0,
    val errorMessage: String? = null
)


