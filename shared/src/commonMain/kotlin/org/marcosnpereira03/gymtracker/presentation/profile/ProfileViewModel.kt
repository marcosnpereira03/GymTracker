package org.marcosnpereira03.gymtracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateMuscleGroupVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.GetPersonalRecordsUseCase
import org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil

/**
 * ViewModel para gestionar el perfil del usuario, estadísticas, récords personales (PRs) y pesajes.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val calculateMuscleGroupVolumeUseCase: CalculateMuscleGroupVolumeUseCase,
    private val calculateWorkoutVolumeUseCase: CalculateWorkoutVolumeUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var cachedWorkouts: List<Workout> = emptyList()
    private var cachedExercises: List<Exercise> = emptyList()

    init {
        viewModelScope.launch {
            authRepository.checkCurrentSession()
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val weightsResult = profileRepository.getBodyWeightLogs()
            val workoutsResult = workoutRepository.getWorkouts()
            val exercisesResult = exerciseRepository.getExercises()

            if (weightsResult.isSuccess && workoutsResult.isSuccess && exercisesResult.isSuccess) {
                val weights = weightsResult.getOrDefault(emptyList())
                cachedWorkouts = workoutsResult.getOrDefault(emptyList())
                cachedExercises = exercisesResult.getOrDefault(emptyList())

                val volumes = computeVolumeForPeriod(_uiState.value.selectedPeriod, cachedWorkouts, cachedExercises)
                val totalPeriodVolume = volumes.sumOf { it.totalVolumeKg }
                val allWorkoutsVolume = cachedWorkouts.sumOf { calculateWorkoutVolumeUseCase(it) }
                val prs = getPersonalRecordsUseCase(cachedWorkouts, cachedExercises)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalWorkoutsCount = cachedWorkouts.size,
                        totalVolumeKg = allWorkoutsVolume,
                        weightLogs = weights,
                        latestWeight = weights.firstOrNull(),
                        personalRecords = prs,
                        muscleGroupVolumes = volumes,
                        totalPeriodVolumeKg = totalPeriodVolume
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar los datos del perfil."
                    )
                }
            }
        }
    }

    fun onSelectTab(tab: ProfileTab) {
        _uiState.update { it.copy(activeTab = tab) }
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

    fun onUpdateProfile(username: String, avatarUrl: String?) {
        val trimmed = username.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre de usuario no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, errorMessage = null, successMessage = null) }
            val result = authRepository.updateProfile(trimmed, avatarUrl)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isSavingProfile = false,
                        currentUser = updatedUser,
                        successMessage = "¡Perfil actualizado con éxito!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSavingProfile = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar perfil."
                    )
                }
            }
        }
    }

    fun onAddWeightLog(weightKg: Double, notes: String?) {
        if (weightKg <= 0.0) return
        viewModelScope.launch {
            val log = BodyWeightLog(
                id = "bw-${DateTimeUtil.now().toEpochMilliseconds()}",
                date = DateTimeUtil.now(),
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

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
            loadProfileData()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun computeVolumeForPeriod(
        period: VolumePeriod,
        workouts: List<Workout>,
        exercises: List<Exercise>
    ): List<org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume> {
        val filteredWorkouts = when (period) {
            VolumePeriod.WEEKLY -> {
                val cutoff = DateTimeUtil.now().minus(7 * 24 * 3600, DateTimeUnit.SECOND)
                workouts.filter { it.date >= cutoff }
            }
            VolumePeriod.MONTHLY -> {
                val cutoff = DateTimeUtil.now().minus(30 * 24 * 3600, DateTimeUnit.SECOND)
                workouts.filter { it.date >= cutoff }
            }
            VolumePeriod.ALL_TIME -> workouts
        }
        return calculateMuscleGroupVolumeUseCase(filteredWorkouts, exercises)
    }
}
