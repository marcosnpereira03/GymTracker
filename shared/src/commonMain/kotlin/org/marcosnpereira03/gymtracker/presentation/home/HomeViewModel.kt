package org.marcosnpereira03.gymtracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase

/**
 * ViewModel para la pantalla principal "Hoy" con Unidirectional Data Flow (UDF).
 */
class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
    private val calculateWorkoutVolumeUseCase: CalculateWorkoutVolumeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val workoutsResult = workoutRepository.getWorkouts()
            val weightsResult = profileRepository.getBodyWeightLogs()

            if (workoutsResult.isSuccess && weightsResult.isSuccess) {
                val workouts = workoutsResult.getOrDefault(emptyList())
                val weights = weightsResult.getOrDefault(emptyList())

                val latest = workouts.firstOrNull()
                val latestVolume = latest?.let { calculateWorkoutVolumeUseCase(it) } ?: 0.0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        latestWorkout = latest,
                        latestWeight = weights.firstOrNull(),
                        recentWorkouts = workouts.take(5),
                        totalVolumeLatestWorkout = latestVolume
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar datos del dashboard. Desliza para reintentar."
                    )
                }
            }
        }
    }
}
