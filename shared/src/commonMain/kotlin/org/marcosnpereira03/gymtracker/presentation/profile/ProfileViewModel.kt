package org.marcosnpereira03.gymtracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateMuscleGroupVolumeUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/**
 * ViewModel para gestionar los pesajes corporales y el desglose de volumen por grupo muscular.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val calculateMuscleGroupVolumeUseCase: CalculateMuscleGroupVolumeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var cachedWorkouts: List<Workout> = emptyList()
    private var cachedExercises: List<Exercise> = emptyList()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val weightsResult = profileRepository.getBodyWeightLogs()
            val workoutsResult = workoutRepository.getWorkouts()
            val exercisesResult = exerciseRepository.getExercises()

            if (weightsResult.isSuccess && workoutsResult.isSuccess && exercisesResult.isSuccess) {
                val weights = weightsResult.getOrDefault(emptyList())
                cachedWorkouts = workoutsResult.getOrDefault(emptyList())
                cachedExercises = exercisesResult.getOrDefault(emptyList())

                val volumes = computeVolumeForPeriod(_uiState.value.selectedPeriod, cachedWorkouts, cachedExercises)
                val totalVolume = volumes.sumOf { it.totalVolumeKg }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        weightLogs = weights,
                        latestWeight = weights.firstOrNull(),
                        muscleGroupVolumes = volumes,
                        totalPeriodVolumeKg = totalVolume
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar datos del perfil."
                    )
                }
            }
        }
    }

    fun onSelectPeriod(period: VolumePeriod) {
        val volumes = computeVolumeForPeriod(period, cachedWorkouts, cachedExercises)
        val total = volumes.sumOf { it.totalVolumeKg }
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                muscleGroupVolumes = volumes,
                totalPeriodVolumeKg = total
            )
        }
    }

    fun onAddWeightLog(weightKg: Double, notes: String?) {
        if (weightKg <= 0.0) return
        viewModelScope.launch {
            val log = BodyWeightLog(
                id = "bw-${org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now().toEpochMilliseconds()}",
                date = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now(),
                weightKg = weightKg,
                notes = notes?.ifBlank { null }
            )
            val result = profileRepository.saveBodyWeightLog(log)
            if (result.isSuccess) {
                loadProfileData()
            }
        }
    }

    fun onDeleteWeightLog(id: String) {
        viewModelScope.launch {
            val result = profileRepository.deleteBodyWeightLog(id)
            if (result.isSuccess) {
                loadProfileData()
            }
        }
    }

    private fun computeVolumeForPeriod(
        period: VolumePeriod,
        workouts: List<Workout>,
        exercises: List<Exercise>
    ): List<org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume> {
        val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
        val daysToFilter = when (period) {
            VolumePeriod.DAILY -> 1
            VolumePeriod.WEEKLY -> 7
            VolumePeriod.MONTHLY -> 30
        }
        val cutoffInstant = now.minus(daysToFilter * 24 * 3600, DateTimeUnit.SECOND)

        val filteredWorkouts = workouts.filter { it.date >= cutoffInstant }
        return calculateMuscleGroupVolumeUseCase(filteredWorkouts, exercises)
    }
}
