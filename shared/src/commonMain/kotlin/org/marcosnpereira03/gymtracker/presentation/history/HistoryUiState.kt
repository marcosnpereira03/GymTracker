package org.marcosnpereira03.gymtracker.presentation.history

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet

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
 * Registro de ejercicio específico en una sesión histórica.
 */
data class ExerciseSessionRecord(
    val workoutId: String,
    val workoutTitle: String,
    val workoutDate: String,
    val sets: List<WorkoutSet>
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
    val selectedExerciseId: String? = null,
    val recordLimit: Int = 5,
    val exerciseRecords: List<ExerciseSessionRecord> = emptyList(),
    val bestPrString: String = "45 kg × 15 reps",
    val errorMessage: String? = null
)

