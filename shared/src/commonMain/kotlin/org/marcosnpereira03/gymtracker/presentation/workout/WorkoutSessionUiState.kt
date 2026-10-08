package org.marcosnpereira03.gymtracker.presentation.workout

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import kotlin.time.Instant

/**
 * Representa una serie en estado de edición interactiva en pantalla.
 */
data class EditableSet(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val weightText: String,
    val repsText: String,
    val rir: Int,
    val estimated1Rm: Double = 0.0,
    val isCompleted: Boolean = false
)

/**
 * Representa una sesión histórica previa de un ejercicio específico para el modal de detalles.
 */
data class ExercisePastSession(
    val workoutId: String,
    val workoutTitle: String,
    val workoutDate: Instant,
    val sets: List<WorkoutSet>
)

/**
 * Estado UI inmutable para la pantalla de carga/edición de sesión de entrenamiento.
 */
data class WorkoutSessionUiState(
    val workoutId: String? = null,
    val title: String = "Entrenamiento",
    val date: Instant? = null,
    val bodyWeightText: String = "",
    val notes: String = "",
    val sets: List<EditableSet> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val selectedExercise: Exercise? = null,
    val historicalWorkouts: List<Workout> = emptyList(),
    val viewingHistoryExerciseId: String? = null,
    val historyLimit: Int = 5,
    val totalVolumeKg: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)
