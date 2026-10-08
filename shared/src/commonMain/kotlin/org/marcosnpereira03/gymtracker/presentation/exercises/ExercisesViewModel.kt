package org.marcosnpereira03.gymtracker.presentation.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.GetExerciseHistoryUseCase
import kotlinx.datetime.Clock

/**
 * ViewModel para el catálogo de ejercicios, récord de 1RM e historial de series previas.
 */
class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val getExerciseHistoryUseCase: GetExerciseHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisesUiState(isLoading = true))
    val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val exercisesResult = exerciseRepository.getExercises()
                val workoutsResult = workoutRepository.getWorkouts()

                if (exercisesResult.isSuccess && workoutsResult.isSuccess) {
                    val exercises = exercisesResult.getOrDefault(emptyList())
                    val workouts = workoutsResult.getOrDefault(emptyList())

                    val cardDataList = exercises.sortedBy { it.name.lowercase() }.map { exercise ->
                        val history = getExerciseHistoryUseCase(exercise.id, workouts)
                        val max1Rm = history.maxOfOrNull { it.estimated1Rm } ?: 0.0
                        ExerciseCardData(
                            exercise = exercise,
                            maxEstimated1Rm = max1Rm,
                            history = history
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            exercises = cardDataList,
                            filteredExercises = applyFilters(cardDataList, it.searchQuery, it.selectedMuscleGroup)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo establecer la conexión con el servidor. Verifica tu conexión a internet."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo establecer la conexión con el servidor. Verifica tu conexión a internet."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredExercises = applyFilters(it.exercises, query, it.selectedMuscleGroup)
            )
        }
    }

    fun onSelectMuscleGroup(muscle: String) {
        _uiState.update {
            it.copy(
                selectedMuscleGroup = muscle,
                filteredExercises = applyFilters(it.exercises, it.searchQuery, muscle)
            )
        }
    }

    fun onToggleExpandExercise(exerciseId: String) {
        _uiState.update {
            val currentExpanded = it.expandedExerciseId
            it.copy(expandedExerciseId = if (currentExpanded == exerciseId) null else exerciseId)
        }
    }

    fun onCreateCustomExercise(name: String, muscleGroup: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newExercise = Exercise(
                id = "custom-ex-${org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now().toEpochMilliseconds()}",
                name = name.trim(),
                muscleGroup = muscleGroup.trim()
            )
            val result = exerciseRepository.createExercise(newExercise)
            if (result.isSuccess) {
                loadExercises()
            }
        }
    }

    private fun applyFilters(
        list: List<ExerciseCardData>,
        query: String,
        muscle: String
    ): List<ExerciseCardData> {
        return list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.exercise.name.contains(query, ignoreCase = true) ||
                item.exercise.muscleGroup.contains(query, ignoreCase = true)

            val matchesMuscle = muscle == "Todos" || item.exercise.muscleGroup.equals(muscle, ignoreCase = true)

            matchesQuery && matchesMuscle
        }
    }
}
