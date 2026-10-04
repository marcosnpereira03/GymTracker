package org.marcosnpereira03.gymtracker.presentation.workout

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import kotlinx.datetime.Instant

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
    val totalVolumeKg: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)
