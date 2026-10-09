package org.marcosnpereira03.gymtracker.presentation.exercises

import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.ExerciseHistoryItem

/**
 * Ejercicio con métricas calculadas para la vista de catálogo.
 */
data class ExerciseCardData(
    val exercise: Exercise,
    val maxEstimated1Rm: Double = 0.0,
    val history: List<ExerciseHistoryItem> = emptyList()
)

/**
 * Estado UI inmutable para la pantalla de ejercicios.
 */
data class ExercisesUiState(
    val isLoading: Boolean = false,
    val exercises: List<ExerciseCardData> = emptyList(),
    val filteredExercises: List<ExerciseCardData> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String = "Todos",
    val muscleGroups: List<String> = listOf("Todos", "Pecho", "Espalda", "Bíceps", "Tríceps", "Hombros", "Antebrazos", "Cuádriceps", "Isquios", "Glúteos", "Gemelos", "Aductores", "Abductores"),
    val expandedExerciseId: String? = null,
    val isCreatingExercise: Boolean = false,
    val errorMessage: String? = null
)
