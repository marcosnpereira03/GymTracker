package org.marcosnpereira03.gymtracker.presentation.history

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout

/**
 * Resumen enriquecido para mostrar en la lista del historial.
 */
data class WorkoutHistoryDetail(
    val workout: Workout,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val exercisesSummary: List<String>
)

/**
 * Estado UI inmutable para la pantalla de Historial.
 */
data class HistoryUiState(
    val isLoading: Boolean = false,
    val workouts: List<WorkoutHistoryDetail> = emptyList(),
    val filteredWorkouts: List<WorkoutHistoryDetail> = emptyList(),
    val searchQuery: String = "",
    val availableExercises: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)
