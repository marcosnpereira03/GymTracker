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
import org.marcosnpereira03.gymtracker.domain.util.UuidUtil
import kotlinx.datetime.Clock
import kotlin.time.Instant

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

    fun onResetSavedSuccess() {
        _uiState.update { it.copy(isSavedSuccess = false) }
    }

    fun initSession(workoutId: String? = null, initialDateString: String? = null) {
        // Resetear inmediatamente isSavedSuccess para evitar navegación no deseada y activar loading
        _uiState.update { it.copy(isLoading = true, isSavedSuccess = false, errorMessage = null) }

        viewModelScope.launch {
            val exercisesResult = if (_uiState.value.availableExercises.isEmpty()) {
                exerciseRepository.getExercises().getOrDefault(emptyList())
            } else {
                _uiState.value.availableExercises
            }
            val firstExercise = exercisesResult.firstOrNull()

            val historicalWorkoutsResult = workoutRepository.getWorkouts().getOrDefault(emptyList())

            if (workoutId != null) {
                // Si ya estamos editando exactamente este entrenamiento, no lo reiniciamos
                if (_uiState.value.workoutId == workoutId && _uiState.value.sets.isNotEmpty()) {
                    _uiState.update { it.copy(availableExercises = exercisesResult, historicalWorkouts = historicalWorkoutsResult, isLoading = false, isSavedSuccess = false) }
                    return@launch
                }

                // Cargar sesión existente para editar
                val workoutResult = workoutRepository.getWorkoutById(workoutId)
                if (workoutResult.isSuccess) {
                    val workout = workoutResult.getOrThrow()
                    val exerciseMap = exercisesResult.associateBy { it.id.lowercase() }

                    val editableSets = workout.sets.map { set ->
                        val ex = exerciseMap[set.exerciseId.lowercase()]
                            ?: exercisesResult.find { it.id.equals(set.exerciseId, ignoreCase = true) }
                        val name = ex?.name ?: "Ejercicio"
                        val oneRm = calculateOneRepMaxUseCase(set.weightKg, set.reps, set.rir)
                        val weightStr = if (set.weightKg > 0.0) {
                            if (set.weightKg % 1.0 == 0.0) "${set.weightKg.toInt()}" else "${set.weightKg}"
                        } else ""
                        EditableSet(
                            id = set.id,
                            exerciseId = ex?.id ?: set.exerciseId,
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
                            historicalWorkouts = historicalWorkoutsResult,
                            totalVolumeKg = calculateWorkoutVolumeUseCase(workout),
                            isSavedSuccess = false,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSavedSuccess = false,
                            errorMessage = "No se pudo cargar el entrenamiento seleccionado."
                        )
                    }
                }
            } else {
                // Modo sesión en vivo / borrador
                // Si ya existe un borrador en curso con series o notas, conservamos el borrador sin borrarlo al navegar
                val hasExistingDraft = _uiState.value.sets.isNotEmpty() ||
                        _uiState.value.notes.isNotBlank() ||
                        _uiState.value.bodyWeightText.isNotBlank()

                if (hasExistingDraft && _uiState.value.workoutId != null) {
                    if (initialDateString != null) {
                        val sessionDate = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.parseDateOrNow(initialDateString)
                        _uiState.update {
                            it.copy(
                                date = sessionDate,
                                availableExercises = exercisesResult,
                                historicalWorkouts = historicalWorkoutsResult,
                                isSavedSuccess = false,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                availableExercises = exercisesResult,
                                historicalWorkouts = historicalWorkoutsResult,
                                isSavedSuccess = false,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    // Inicialización de una sesión en blanco con UUID estándar
                    val sessionDate = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.parseDateOrNow(initialDateString)
                    _uiState.update {
                        it.copy(
                            workoutId = UuidUtil.randomUuid(),
                            title = "Entrenamiento",
                            date = sessionDate,
                            availableExercises = exercisesResult,
                            selectedExercise = firstExercise,
                            historicalWorkouts = historicalWorkoutsResult,
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

    fun onMoveExerciseUp(exerciseId: String) {
        val currentSets = _uiState.value.sets
        val orderedExerciseIds = currentSets.map { it.exerciseId }.distinct().toMutableList()
        val index = orderedExerciseIds.indexOf(exerciseId)
        if (index > 0) {
            val temp = orderedExerciseIds[index]
            orderedExerciseIds[index] = orderedExerciseIds[index - 1]
            orderedExerciseIds[index - 1] = temp

            val setsByExercise = currentSets.groupBy { it.exerciseId }
            val reorderedSets = orderedExerciseIds.flatMap { id -> setsByExercise[id] ?: emptyList() }
            _uiState.update { it.copy(sets = reorderedSets) }
        }
    }

    fun onMoveExerciseDown(exerciseId: String) {
        val currentSets = _uiState.value.sets
        val orderedExerciseIds = currentSets.map { it.exerciseId }.distinct().toMutableList()
        val index = orderedExerciseIds.indexOf(exerciseId)
        if (index != -1 && index < orderedExerciseIds.size - 1) {
            val temp = orderedExerciseIds[index]
            orderedExerciseIds[index] = orderedExerciseIds[index + 1]
            orderedExerciseIds[index + 1] = temp

            val setsByExercise = currentSets.groupBy { it.exerciseId }
            val reorderedSets = orderedExerciseIds.flatMap { id -> setsByExercise[id] ?: emptyList() }
            _uiState.update { it.copy(sets = reorderedSets) }
        }
    }

    fun onOpenExerciseHistory(exerciseId: String) {
        _uiState.update { it.copy(viewingHistoryExerciseId = exerciseId) }
    }

    fun onCloseExerciseHistory() {
        _uiState.update { it.copy(viewingHistoryExerciseId = null) }
    }

    fun onSetHistoryLimit(limit: Int) {
        _uiState.update { it.copy(historyLimit = limit) }
    }

    /**
     * Obtiene las sesiones históricas previas donde se realizó el ejercicio especificado.
     */
    fun getPastSessionsForExercise(exerciseId: String, limit: Int): List<ExercisePastSession> {
        val state = _uiState.value
        val currentWorkoutId = state.workoutId

        // Excluir el workout actual si está en edición para no duplicar datos
        val pastWorkouts = state.historicalWorkouts.filter { workout ->
            workout.id != currentWorkoutId && workout.sets.any { it.exerciseId == exerciseId }
        }

        return pastWorkouts
            .take(limit)
            .map { workout ->
                val exerciseSets = workout.sets
                    .filter { it.exerciseId == exerciseId }
                    .sortedBy { it.setNumber }

                ExercisePastSession(
                    workoutId = workout.id,
                    workoutTitle = workout.title,
                    workoutDate = workout.date,
                    sets = exerciseSets
                )
            }
    }

    /**
     * Obtiene el total de sesiones históricas disponibles para el ejercicio.
     */
    fun getTotalPastSessionsCount(exerciseId: String): Int {
        val state = _uiState.value
        val currentWorkoutId = state.workoutId
        return state.historicalWorkouts.count { workout ->
            workout.id != currentWorkoutId && workout.sets.any { it.exerciseId == exerciseId }
        }
    }

    /**
     * Obtiene el mejor récord histórico registrado (mayor peso y reps) para el ejercicio.
     */
    fun getBestRecordForExercise(exerciseId: String): WorkoutSet? {
        val state = _uiState.value
        val allSets = state.historicalWorkouts.flatMap { it.sets }.filter { it.exerciseId == exerciseId }
        if (allSets.isEmpty()) return null

        return allSets.maxWithOrNull(
            compareBy<WorkoutSet> { it.weightKg }
                .thenBy { it.reps }
        )
    }

    /**
     * Obtiene el resumen de la última vez que se realizó el ejercicio: fecha formateada y serie destacada.
     */
    fun getLastSessionSummary(exerciseId: String): Pair<String, String>? {
        val pastSessions = getPastSessionsForExercise(exerciseId, limit = 1)
        val lastSession = pastSessions.firstOrNull() ?: return null
        val bestOrFirstSet = lastSession.sets.maxByOrNull { it.weightKg } ?: lastSession.sets.firstOrNull() ?: return null

        val dateStr = lastSession.workoutDate.toString().substringBefore("T")
        val weightFormatted = if (bestOrFirstSet.weightKg % 1.0 == 0.0) "${bestOrFirstSet.weightKg.toInt()}" else "${bestOrFirstSet.weightKg}"
        val summary = "$weightFormatted kg × ${bestOrFirstSet.reps} (RIR ${bestOrFirstSet.rir})"

        return Pair(dateStr, summary)
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

    private fun getDefaultSetValues(exerciseId: String, currentSets: List<EditableSet>): Triple<String, String, Int> {
        val previousSetOfSameExercise = currentSets.lastOrNull { it.exerciseId == exerciseId }
        if (previousSetOfSameExercise != null) {
            return Triple(
                previousSetOfSameExercise.weightText,
                previousSetOfSameExercise.repsText,
                previousSetOfSameExercise.rir
            )
        }

        val pastSession = getPastSessionsForExercise(exerciseId, limit = 1).firstOrNull()
        val pastSet = pastSession?.sets?.firstOrNull()
        if (pastSet != null) {
            val weightStr = if (pastSet.weightKg % 1.0 == 0.0) "${pastSet.weightKg.toInt()}" else "${pastSet.weightKg}"
            return Triple(
                weightStr,
                pastSet.reps.toString(),
                pastSet.rir.coerceIn(0, 10)
            )
        }

        return Triple("60", "10", 2)
    }

    fun onAddSet() {
        val selected = _uiState.value.selectedExercise ?: return
        val currentSets = _uiState.value.sets
        val exerciseSetsCount = currentSets.count { it.exerciseId == selected.id }
        
        val (defaultWeight, defaultReps, defaultRir) = getDefaultSetValues(selected.id, currentSets)

        val w = defaultWeight.toDoubleOrNull() ?: 0.0
        val r = defaultReps.toIntOrNull() ?: 0
        val oneRm = calculateOneRepMaxUseCase(w, r, defaultRir)

        val newSet = EditableSet(
            id = UuidUtil.randomUuid(),
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
        
        val (defaultWeight, defaultReps, defaultRir) = getDefaultSetValues(exerciseId, currentSets)

        val w = defaultWeight.toDoubleOrNull() ?: 0.0
        val r = defaultReps.toIntOrNull() ?: 0
        val oneRm = calculateOneRepMaxUseCase(w, r, defaultRir)

        val newSet = EditableSet(
            id = UuidUtil.randomUuid(),
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
                workoutId = UuidUtil.randomUuid(),
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
        val workoutId = UuidUtil.ensureUuid(state.workoutId)
        val workoutDate = state.date ?: org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()

        if (state.sets.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes añadir al menos una serie antes de guardar el entrenamiento."
                )
            }
            return
        }

        // Si el usuario marcó series completadas, tomamos esas; si no, tomamos todas las series cargadas
        val completedSets = state.sets.filter { it.isCompleted }
        val setsToSave = if (completedSets.isNotEmpty()) {
            completedSets
        } else {
            val filledSets = state.sets.filter { it.weightText.isNotBlank() || it.repsText.isNotBlank() }
            if (filledSets.isNotEmpty()) filledSets else state.sets
        }

        val domainSets = setsToSave.mapIndexed { index, s ->
            WorkoutSet(
                id = UuidUtil.ensureUuid(s.id),
                workoutId = workoutId,
                exerciseId = UuidUtil.ensureUuid(s.exerciseId),
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
                val now = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.now()
                val exercises = _uiState.value.availableExercises
                val updatedHistory = workoutRepository.getWorkouts().getOrDefault(emptyList())

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        workoutId = UuidUtil.randomUuid(),
                        title = "Entrenamiento",
                        date = now,
                        notes = "",
                        bodyWeightText = "",
                        sets = emptyList(),
                        selectedExercise = exercises.firstOrNull(),
                        historicalWorkouts = updatedHistory,
                        totalVolumeKg = 0.0,
                        errorMessage = null
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
