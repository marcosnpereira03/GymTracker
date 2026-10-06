package org.marcosnpereira03.gymtracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateMuscleGroupVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.GetPersonalRecordsUseCase
import org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil
import kotlin.math.round

/**
 * ViewModel para gestionar el perfil del usuario, estadísticas temporales por período, récords personales (PRs) y pesajes.
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
    private var cachedWeights: List<BodyWeightLog> = emptyList()

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
                cachedWeights = weightsResult.getOrDefault(emptyList()).sortedByDescending { it.date }
                cachedWorkouts = workoutsResult.getOrDefault(emptyList())
                cachedExercises = exercisesResult.getOrDefault(emptyList()).sortedBy { it.name.lowercase() }

                val periodInfo = calculatePeriodRange(_uiState.value.selectedPeriod, _uiState.value.periodOffset)
                val volumes = computeVolumeForRange(periodInfo.first, periodInfo.second, cachedWorkouts, cachedExercises)
                val totalPeriodVolume = volumes.sumOf { it.totalVolumeKg }
                val allWorkoutsVolume = cachedWorkouts.sumOf { calculateWorkoutVolumeUseCase(it) }
                val prs = getPersonalRecordsUseCase(cachedWorkouts, cachedExercises)

                val weightPeriodInfo = calculatePeriodRange(_uiState.value.weightPeriod, _uiState.value.weightPeriodOffset)
                val filteredWeights = filterWeightsForRange(weightPeriodInfo.first, weightPeriodInfo.second, cachedWeights)

                val latestW = cachedWeights.firstOrNull()
                val prevW = cachedWeights.getOrNull(1)
                val oldestW = cachedWeights.lastOrNull()
                val totalLost = if (latestW != null && oldestW != null && oldestW != latestW) {
                    round((oldestW.weightKg - latestW.weightKg) * 10.0) / 10.0
                } else null

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalWorkoutsCount = cachedWorkouts.size,
                        totalVolumeKg = allWorkoutsVolume,
                        weightLogs = cachedWeights,
                        filteredWeightLogs = filteredWeights,
                        latestWeight = latestW,
                        previousWeight = prevW,
                        totalWeightLostKg = totalLost,
                        personalRecords = prs,
                        muscleGroupVolumes = volumes,
                        totalPeriodVolumeKg = totalPeriodVolume,
                        periodRangeLabel = periodInfo.third,
                        weightPeriodRangeLabel = weightPeriodInfo.third
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
        _uiState.update { it.copy(selectedPeriod = period, periodOffset = 0) }
        recalculatePeriodVolumes()
    }

    fun onNavigatePreviousPeriod() {
        if (_uiState.value.selectedPeriod == VolumePeriod.ALL_TIME) return
        _uiState.update { it.copy(periodOffset = it.periodOffset - 1) }
        recalculatePeriodVolumes()
    }

    fun onNavigateNextPeriod() {
        if (!_uiState.value.canNavigateForward) return
        _uiState.update { it.copy(periodOffset = it.periodOffset + 1) }
        recalculatePeriodVolumes()
    }

    private fun recalculatePeriodVolumes() {
        val periodInfo = calculatePeriodRange(_uiState.value.selectedPeriod, _uiState.value.periodOffset)
        val volumes = computeVolumeForRange(periodInfo.first, periodInfo.second, cachedWorkouts, cachedExercises)
        val total = volumes.sumOf { it.totalVolumeKg }
        _uiState.update {
            it.copy(
                muscleGroupVolumes = volumes,
                totalPeriodVolumeKg = total,
                periodRangeLabel = periodInfo.third
            )
        }
    }

    fun onSelectWeightPeriod(period: VolumePeriod) {
        _uiState.update { it.copy(weightPeriod = period, weightPeriodOffset = 0) }
        recalculateWeightPeriodLogs()
    }

    fun onNavigatePreviousWeightPeriod() {
        if (_uiState.value.weightPeriod == VolumePeriod.ALL_TIME) return
        _uiState.update { it.copy(weightPeriodOffset = it.weightPeriodOffset - 1) }
        recalculateWeightPeriodLogs()
    }

    fun onNavigateNextWeightPeriod() {
        if (!_uiState.value.canNavigateWeightForward) return
        _uiState.update { it.copy(weightPeriodOffset = it.weightPeriodOffset + 1) }
        recalculateWeightPeriodLogs()
    }

    private fun recalculateWeightPeriodLogs() {
        val weightPeriodInfo = calculatePeriodRange(_uiState.value.weightPeriod, _uiState.value.weightPeriodOffset)
        val filteredWeights = filterWeightsForRange(weightPeriodInfo.first, weightPeriodInfo.second, cachedWeights)
        _uiState.update {
            it.copy(
                filteredWeightLogs = filteredWeights,
                weightPeriodRangeLabel = weightPeriodInfo.third
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

    private fun calculatePeriodRange(period: VolumePeriod, offset: Int): Triple<Instant?, Instant?, String> {
        val now = DateTimeUtil.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date

        return when (period) {
            VolumePeriod.DAILY -> {
                val targetDate = today.plus(offset, DateTimeUnit.DAY)
                val start = DateTimeUtil.parseDateOrNow("${targetDate}T00:00:00Z")
                val end = DateTimeUtil.parseDateOrNow("${targetDate}T23:59:59Z")
                val label = when (offset) {
                    0 -> "Hoy, ${DateTimeUtil.formatShortDate(start)}"
                    -1 -> "Ayer, ${DateTimeUtil.formatShortDate(start)}"
                    else -> DateTimeUtil.formatFullShortDate(start)
                }
                Triple(start, end, label)
            }

            VolumePeriod.WEEKLY -> {
                val endDay = today.plus(offset * 7, DateTimeUnit.DAY)
                val startDay = endDay.minus(6, DateTimeUnit.DAY)
                val start = DateTimeUtil.parseDateOrNow("${startDay}T00:00:00Z")
                val end = DateTimeUtil.parseDateOrNow("${endDay}T23:59:59Z")
                val startLabel = DateTimeUtil.formatShortDate(start)
                val endLabel = DateTimeUtil.formatShortDate(end)
                Triple(start, end, "$startLabel - $endLabel")
            }

            VolumePeriod.MONTHLY -> {
                val targetDate = today.plus(offset, DateTimeUnit.MONTH)
                val month = targetDate.month.ordinal + 1
                val year = targetDate.year
                val monthName = DateTimeUtil.shortMonthName(month)
                val daysInMonth = when (month) {
                    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                    4, 6, 9, 11 -> 30
                    else -> 31
                }
                val start = DateTimeUtil.parseDateOrNow("${year}-${month.toString().padStart(2, '0')}-01T00:00:00Z")
                val end = DateTimeUtil.parseDateOrNow("${year}-${month.toString().padStart(2, '0')}-${daysInMonth.toString().padStart(2, '0')}T23:59:59Z")
                val label = "1 $monthName - $daysInMonth $monthName, $year"
                Triple(start, end, label)
            }

            VolumePeriod.ALL_TIME -> {
                Triple(null, null, "Histórico Completo")
            }
        }
    }

    private fun computeVolumeForRange(
        start: Instant?,
        end: Instant?,
        workouts: List<Workout>,
        exercises: List<Exercise>
    ): List<MuscleGroupVolume> {
        val filtered = if (start != null && end != null) {
            workouts.filter { it.date in start..end }
        } else {
            workouts
        }
        return calculateMuscleGroupVolumeUseCase(filtered, exercises)
    }

    private fun filterWeightsForRange(
        start: Instant?,
        end: Instant?,
        weights: List<BodyWeightLog>
    ): List<BodyWeightLog> {
        return if (start != null && end != null) {
            weights.filter { it.date in start..end }
        } else {
            weights
        }
    }
}
