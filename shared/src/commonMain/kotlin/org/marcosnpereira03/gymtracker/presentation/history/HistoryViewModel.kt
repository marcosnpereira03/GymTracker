package org.marcosnpereira03.gymtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase

/**
 * ViewModel para gestionar el historial completo de entrenamientos con UDF.
 */
class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val calculateWorkoutVolumeUseCase: CalculateWorkoutVolumeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val workoutsResult = workoutRepository.getWorkouts()
            val exercisesResult = exerciseRepository.getExercises()

            if (workoutsResult.isSuccess && exercisesResult.isSuccess) {
                val workouts = workoutsResult.getOrDefault(emptyList())
                val exercises = exercisesResult.getOrDefault(emptyList())
                val exerciseMap = exercises.associateBy { it.id }

                val details = workouts.map { workout ->
                    val volume = calculateWorkoutVolumeUseCase(workout)
                    val exerciseNames = workout.sets
                        .mapNotNull { set -> exerciseMap[set.exerciseId]?.name }
                        .distinct()

                    WorkoutHistoryDetail(
                        workout = workout,
                        totalVolumeKg = volume,
                        totalSets = workout.sets.size,
                        exercisesSummary = exerciseNames
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workouts = details,
                        filteredWorkouts = filterList(details, it.searchQuery),
                        availableExercises = exercises
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar historial de entrenamientos."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredWorkouts = filterList(it.workouts, query)
            )
        }
    }

    fun onDeleteWorkout(workoutId: String) {
        viewModelScope.launch {
            val result = workoutRepository.deleteWorkout(workoutId)
            if (result.isSuccess) {
                loadHistory()
            }
        }
    }

    private fun filterList(list: List<WorkoutHistoryDetail>, query: String): List<WorkoutHistoryDetail> {
        if (query.isBlank()) return list
        return list.filter {
            it.workout.title.contains(query, ignoreCase = true) ||
            it.workout.date.toString().contains(query, ignoreCase = true) ||
            it.exercisesSummary.any { ex -> ex.contains(query, ignoreCase = true) }
        }
    }
}
