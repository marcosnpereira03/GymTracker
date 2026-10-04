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

    fun initSession(workoutId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val exercisesResult = exerciseRepository.getExercises()
            val exercises = exercisesResult.getOrDefault(emptyList())
            val firstExercise = exercises.firstOrNull()

            if (workoutId != null) {
                // Cargar sesión existente para editar
                val workoutResult = workoutRepository.getWorkoutById(workoutId)
                if (workoutResult.isSuccess) {
                    val workout = workoutResult.getOrThrow()
                    val exerciseMap = exercises.associateBy { it.id }

                    val editableSets = workout.sets.map { set ->
                        val ex = exerciseMap[set.exerciseId]
                        val name = ex?.name ?: "Ejercicio"
                        val oneRm = calculateOneRepMaxUseCase(set.weightKg, set.reps, set.rir)
                        EditableSet(
                            id = set.id,
                            exerciseId = set.exerciseId,
                            exerciseName = name,
                            setNumber = set.setNumber,
                            weightText = if (set.weightKg > 0.0) set.weightKg.toString() else "",
                            repsText = if (set.reps > 0) set.reps.toString() else "",
                            rir = set.rir,
                            estimated1Rm = oneRm
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
                            availableExercises = exercises,
                            selectedExercise = firstExercise,
                            totalVolumeKg = calculateWorkoutVolumeUseCase(workout),
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
                // Iniciar nueva sesión con fecha actual
                val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
                _uiState.update {
                    it.copy(
                        workoutId = "w-${now.toEpochMilliseconds()}",
                        title = "Entrenamiento",
                        date = now,
                        availableExercises = exercises,
                        selectedExercise = firstExercise,
                        sets = emptyList(),
                        totalVolumeKg = 0.0,
                        isLoading = false
                    )
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

    fun onAddSet() {
        val selected = _uiState.value.selectedExercise ?: return
        val currentSets = _uiState.value.sets
        val exerciseSetsCount = currentSets.count { it.exerciseId == selected.id }
        
        // Tomamos como sugerencia el peso de la serie anterior para agilizar la carga
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
            estimated1Rm = oneRm
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

        val domainSets = state.sets.mapIndexed { index, s ->
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
                _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo guardar la sesión en el servidor. Guardado localmente."
                    )
                }
            }
        }
    }

    private fun calculateTotalVolume(sets: List<EditableSet>): Double {
        val domainSets = sets.map {
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
        return calculateWorkoutVolumeUseCase(domainSets)
    }
}
