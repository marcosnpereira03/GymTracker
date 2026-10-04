package org.marcosnpereira03.gymtracker.presentation.home

import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Estado UI inmutable para la pantalla principal (Hoy).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val latestWorkout: Workout? = null,
    val latestWeight: BodyWeightLog? = null,
    val recentWorkouts: List<Workout> = emptyList(),
    val totalVolumeLatestWorkout: Double = 0.0,
    val errorMessage: String? = null
)
