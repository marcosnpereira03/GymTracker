package org.marcosnpereira03.gymtracker.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateOneRepMaxUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * ViewModel para gestionar la creación y edición de sesiones y series con UDF.
 */
class WorkoutSessionViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val calculateOneRepMaxUseCase: CalculateOneRepMaxUseCase,
    private val calculateWorkoutVolumeUseCase: CalculateWorkoutVolumeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionUiState(isLoading = true))
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    fun initSession(workoutId: String? = null, initialDateString: String? = null) {
        viewModelScope.launch {
            val exercisesResult = if (_uiState.value.availableExercises.isEmpty()) {
                exerciseRepository.getExercises().getOrDefault(emptyList())
            } else {
                _uiState.value.availableExercises
            }
            val firstExercise = exercisesResult.firstOrNull()

            if (workoutId != null) {
                // Si ya estamos editando exactamente este entrenamiento, no lo reiniciamos
                if (_uiState.value.workoutId == workoutId && _uiState.value.sets.isNotEmpty()) {
                    _uiState.update { it.copy(availableExercises = exercisesResult, isLoading = false) }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Cargar sesión existente para editar
                val workoutResult = workoutRepository.getWorkoutById(workoutId)
                if (workoutResult.isSuccess) {
                    val workout = workoutResult.getOrThrow()
                    val exerciseMap = exercisesResult.associateBy { it.id }

                    val editableSets = workout.sets.map { set ->
                        val ex = exerciseMap[set.exerciseId]
                        val name = ex?.name ?: "Ejercicio"
                        val oneRm = calculateOneRepMaxUseCase(set.weightKg, set.reps, set.rir)
                        val weightStr = if (set.weightKg > 0.0) {
                            if (set.weightKg % 1.0 == 0.0) "${set.weightKg.toInt()}" else "${set.weightKg}"
                        } else ""
                        EditableSet(
                            id = set.id,
                            exerciseId = set.exerciseId,
                            exerciseName = name,
                            setNumber = set.setNumber,
                            weightText = weightStr,
                            repsText = if (set.reps > 0) set.reps.toString() else "",
                            rir = set.rir,
                            estimated1Rm = oneRm,
                            isCompleted = true // Series ya existentes en base de datos inician completadas
                        )
                    }

                    _uiState.update {
                        it.copy(
                            workoutId = workout.id,
                            title = workout.title,
                            date = workout.date,
                            bodyWeightText = workout.bodyWeight?.toString() ?: "",
                            notes = workout.notes ?: "",
                            sets = editableSets,
                            availableExercises = exercisesResult,
                            selectedExercise = firstExercise,
                            totalVolumeKg = calculateWorkoutVolumeUseCase(workout),
                            isSavedSuccess = false,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo cargar el entrenamiento seleccionado."
                        )
                    }
                }
            } else {
                // Modo sesión en vivo / borrador
                // Si la sesión anterior ya fue guardada con éxito, limpiamos para una nueva
                if (_uiState.value.isSavedSuccess) {
                    val sessionDate = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.parseDateOrNow(initialDateString)
                    val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
                    _uiState.update {
                        it.copy(
                            workoutId = "w-${now.toEpochMilliseconds()}",
                            title = "Entrenamiento",
                            date = sessionDate,
                            availableExercises = exercisesResult,
                            selectedExercise = firstExercise,
                            sets = emptyList(),
                            bodyWeightText = "",
                            notes = "",
                            totalVolumeKg = 0.0,
                            isSavedSuccess = false,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    return@launch
                }

                // Si ya existe un borrador en curso con series o notas, conservamos el borrador sin borrarlo al navegar
                val hasExistingDraft = _uiState.value.sets.isNotEmpty() ||
                        _uiState.value.notes.isNotBlank() ||
                        _uiState.value.bodyWeightText.isNotBlank() ||
                        (_uiState.value.workoutId != null && _uiState.value.workoutId!!.startsWith("w-"))

                if (hasExistingDraft && _uiState.value.workoutId != null) {
                    if (initialDateString != null) {
                        val sessionDate = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.parseDateOrNow(initialDateString)
                        _uiState.update {
                            it.copy(
                                date = sessionDate,
                                availableExercises = exercisesResult,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                availableExercises = exercisesResult,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    // Primera inicialización de una sesión en blanco
                    val sessionDate = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.parseDateOrNow(initialDateString)
                    val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
                    _uiState.update {
                        it.copy(
                            workoutId = "w-${now.toEpochMilliseconds()}",
                            title = "Entrenamiento",
                            date = sessionDate,
                            availableExercises = exercisesResult,
                            selectedExercise = firstExercise,
                            sets = emptyList(),
                            bodyWeightText = "",
                            notes = "",
                            totalVolumeKg = 0.0,
                            isSavedSuccess = false,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onBodyWeightChange(weightStr: String) {
        _uiState.update { it.copy(bodyWeightText = weightStr) }
    }

    fun onNotesChange(notesStr: String) {
        _uiState.update { it.copy(notes = notesStr) }
    }

    fun onSelectExercise(exercise: Exercise) {
        _uiState.update { it.copy(selectedExercise = exercise) }
    }

    fun onClearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onToggleSetCompleted(setId: String) {
        val updatedSets = _uiState.value.sets.map { set ->
            if (set.id == setId) {
                set.copy(isCompleted = !set.isCompleted)
            } else set
        }

        val volume = calculateTotalVolume(updatedSets)

        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onAddSet() {
        val selected = _uiState.value.selectedExercise ?: return
        val currentSets = _uiState.value.sets
        val exerciseSetsCount = currentSets.count { it.exerciseId == selected.id }
        
        val previousSetOfSameExercise = currentSets.lastOrNull { it.exerciseId == selected.id }
        val defaultWeight = previousSetOfSameExercise?.weightText ?: "60"
        val defaultReps = previousSetOfSameExercise?.repsText ?: "10"
        val defaultRir = previousSetOfSameExercise?.rir ?: 2

        val w = defaultWeight.toDoubleOrNull() ?: 0.0
        val r = defaultReps.toIntOrNull() ?: 0
        val oneRm = calculateOneRepMaxUseCase(w, r, defaultRir)

        val newSet = EditableSet(
            id = "s-${org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now().toEpochMilliseconds()}-${currentSets.size + 1}",
            exerciseId = selected.id,
            exerciseName = selected.name,
            setNumber = exerciseSetsCount + 1,
            weightText = defaultWeight,
            repsText = defaultReps,
            rir = defaultRir,
            estimated1Rm = oneRm,
            isCompleted = false // Inicia en gris (pendiente)
        )

        val updatedSets = currentSets + newSet
        val volume = calculateTotalVolume(updatedSets)

        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onAddSetToExercise(exerciseId: String) {
        val currentSets = _uiState.value.sets
        val exerciseSets = currentSets.filter { it.exerciseId == exerciseId }
        val exerciseName = exerciseSets.firstOrNull()?.exerciseName 
            ?: _uiState.value.availableExercises.firstOrNull { it.id == exerciseId }?.name 
            ?: "Ejercicio"
        
        val lastSet = exerciseSets.lastOrNull()
        val defaultWeight = lastSet?.weightText ?: "50"
        val defaultReps = lastSet?.repsText ?: "10"
        val defaultRir = lastSet?.rir ?: 1

        val w = defaultWeight.toDoubleOrNull() ?: 0.0
        val r = defaultReps.toIntOrNull() ?: 0
        val oneRm = calculateOneRepMaxUseCase(w, r, defaultRir)

        val newSet = EditableSet(
            id = "s-${org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now().toEpochMilliseconds()}-${currentSets.size + 1}",
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            setNumber = exerciseSets.size + 1,
            weightText = defaultWeight,
            repsText = defaultReps,
            rir = defaultRir,
            estimated1Rm = oneRm,
            isCompleted = false // Inicia en gris (pendiente)
        )

        val updatedSets = currentSets + newSet
        val volume = calculateTotalVolume(updatedSets)

        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onDeleteExercise(exerciseId: String) {
        val updatedSets = _uiState.value.sets.filterNot { it.exerciseId == exerciseId }
        val volume = calculateTotalVolume(updatedSets)
        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onResetSession() {
        val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
        val firstEx = _uiState.value.availableExercises.firstOrNull()
        _uiState.update {
            it.copy(
                workoutId = "w-${now.toEpochMilliseconds()}",
                title = "Entrenamiento",
                date = now,
                notes = "",
                bodyWeightText = "",
                sets = emptyList(),
                selectedExercise = firstEx,
                totalVolumeKg = 0.0,
                isSavedSuccess = false,
                errorMessage = null
            )
        }
    }

    fun onUpdateSet(setId: String, weightText: String, repsText: String, rir: Int) {
        val updatedSets = _uiState.value.sets.map { set ->
            if (set.id == setId) {
                val w = weightText.toDoubleOrNull() ?: 0.0
                val r = repsText.toIntOrNull() ?: 0
                val oneRm = calculateOneRepMaxUseCase(w, r, rir)
                set.copy(
                    weightText = weightText,
                    repsText = repsText,
                    rir = rir,
                    estimated1Rm = oneRm
                )
            } else set
        }

        val volume = calculateTotalVolume(updatedSets)

        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onDeleteSet(setId: String) {
        val updatedSets = _uiState.value.sets.filterNot { it.id == setId }
        val volume = calculateTotalVolume(updatedSets)
        _uiState.update {
            it.copy(
                sets = updatedSets,
                totalVolumeKg = volume
            )
        }
    }

    fun onSaveWorkout() {
        val state = _uiState.value
        val workoutId = state.workoutId ?: "w-${org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now().toEpochMilliseconds()}"
        val workoutDate = state.date ?: org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()

        // Filtrar exclusivamente las series que están marcadas en verde (isCompleted == true)
        val completedSets = state.sets.filter { it.isCompleted }
        if (completedSets.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes marcar al menos una serie como lista (en verde) antes de guardar el entrenamiento."
                )
            }
            return
        }

        val domainSets = completedSets.mapIndexed { index, s ->
            WorkoutSet(
                id = s.id,
                workoutId = workoutId,
                exerciseId = s.exerciseId,
                setNumber = index + 1,
                weightKg = s.weightText.toDoubleOrNull() ?: 0.0,
                reps = s.repsText.toIntOrNull() ?: 0,
                rir = s.rir
            )
        }

        val workout = Workout(
            id = workoutId,
            title = state.title.ifBlank { "Entrenamiento" },
            date = workoutDate,
            bodyWeight = state.bodyWeightText.toDoubleOrNull(),
            notes = state.notes.ifBlank { null },
            sets = domainSets
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val saveResult = workoutRepository.saveWorkout(workout)

            if (saveResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        sets = emptyList(),
                        notes = "",
                        bodyWeightText = ""
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo guardar la sesión en el servidor. Intenta nuevamente."
                    )
                }
            }
        }
    }

    private fun calculateTotalVolume(sets: List<EditableSet>): Double {
        val completedDomainSets = sets.filter { it.isCompleted }.map {
            WorkoutSet(
                id = it.id,
                workoutId = "",
                exerciseId = it.exerciseId,
                setNumber = it.setNumber,
                weightKg = it.weightText.toDoubleOrNull() ?: 0.0,
                reps = it.repsText.toIntOrNull() ?: 0,
                rir = it.rir
            )
        }
        return calculateWorkoutVolumeUseCase(completedDomainSets)
    }
}
