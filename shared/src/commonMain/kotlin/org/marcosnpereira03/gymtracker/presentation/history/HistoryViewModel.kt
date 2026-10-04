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

                val defaultExercise = _uiState.value.selectedExerciseId?.let { id -> exercises.firstOrNull { it.id == id } }
                    ?: exercises.firstOrNull()

                val (records, prString) = computeExerciseRecords(workouts, defaultExercise?.id, _uiState.value.recordLimit)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workouts = details,
                        filteredWorkouts = filterList(details, it.searchQuery),
                        availableExercises = exercises,
                        selectedExerciseId = defaultExercise?.id,
                        exerciseRecords = records,
                        bestPrString = prString
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

    fun onSelectExercise(exerciseId: String) {
        val workouts = _uiState.value.workouts.map { it.workout }
        val (records, prString) = computeExerciseRecords(workouts, exerciseId, _uiState.value.recordLimit)
        _uiState.update {
            it.copy(
                selectedExerciseId = exerciseId,
                exerciseRecords = records,
                bestPrString = prString
            )
        }
    }

    fun onSelectRecordLimit(limit: Int) {
        val workouts = _uiState.value.workouts.map { it.workout }
        val (records, prString) = computeExerciseRecords(workouts, _uiState.value.selectedExerciseId, limit)
        _uiState.update {
            it.copy(
                recordLimit = limit,
                exerciseRecords = records,
                bestPrString = prString
            )
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

    private fun computeExerciseRecords(
        workouts: List<org.marcosnpereira03.gymtracker.domain.model.Workout>,
        exerciseId: String?,
        limit: Int
    ): Pair<List<ExerciseSessionRecord>, String?> {
        if (exerciseId == null) return Pair(emptyList(), null)

        val matchedRecords = mutableListOf<ExerciseSessionRecord>()
        var bestSet: org.marcosnpereira03.gymtracker.domain.model.WorkoutSet? = null

        workouts.sortedByDescending { it.date }.forEach { workout ->
            val matchingSets = workout.sets.filter { it.exerciseId == exerciseId }
            if (matchingSets.isNotEmpty()) {
                val dateStr = workout.date.toString().substringBefore("T")
                matchedRecords.add(
                    ExerciseSessionRecord(
                        workoutId = workout.id,
                        workoutTitle = workout.title,
                        workoutDate = dateStr,
                        sets = matchingSets
                    )
                )

                matchingSets.forEach { set ->
                    if (bestSet == null) {
                        bestSet = set
                    } else {
                        val currentBest = bestSet!!
                        if (set.weightKg > currentBest.weightKg ||
                            (set.weightKg == currentBest.weightKg && set.reps > currentBest.reps)
                        ) {
                            bestSet = set
                        }
                    }
                }
            }
        }

        val limitedRecords = matchedRecords.take(limit)
        val prDisplay = bestSet?.let {
            val weightStr = if (it.weightKg % 1.0 == 0.0) "${it.weightKg.toInt()}" else "${it.weightKg}"
            "$weightStr kg × ${it.reps} reps"
        }
        return Pair(limitedRecords, prDisplay)
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

