package org.marcosnpereira03.gymtracker.presentation.workout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.model.WorkoutSet
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateOneRepMaxUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeExercises = listOf(
        Exercise(id = "e1", name = "Press Banca", muscleGroup = "Pecho"),
        Exercise(id = "e2", name = "Sentadilla", muscleGroup = "Piernas")
    )

    private val fakeWorkoutRepository = object : WorkoutRepository {
        val savedWorkouts = mutableListOf<Workout>()
        override suspend fun getWorkouts(): Result<List<Workout>> = Result.success(savedWorkouts)
        override suspend fun getWorkoutById(id: String): Result<Workout> {
            val w = savedWorkouts.find { it.id == id }
            return if (w != null) Result.success(w) else Result.failure(NoSuchElementException())
        }
        override suspend fun saveWorkout(workout: Workout): Result<Workout> {
            savedWorkouts.removeAll { it.id == workout.id }
            savedWorkouts.add(workout)
            return Result.success(workout)
        }
        override suspend fun deleteWorkout(id: String): Result<Unit> {
            savedWorkouts.removeAll { it.id == id }
            return Result.success(Unit)
        }
    }

    private val fakeExerciseRepository = object : ExerciseRepository {
        override suspend fun getExercises(): Result<List<Exercise>> = Result.success(fakeExercises)
        override suspend fun searchExercises(query: String): Result<List<Exercise>> = Result.success(fakeExercises.filter { it.name.contains(query, ignoreCase = true) })
        override suspend fun getExerciseById(id: String): Result<Exercise> = Result.success(fakeExercises.first { it.id == id })
        override suspend fun createExercise(exercise: Exercise): Result<Exercise> = Result.success(exercise)
        override suspend fun updateExercise(exercise: Exercise): Result<Exercise> = Result.success(exercise)
        override suspend fun deleteExercise(id: String): Result<Unit> = Result.success(Unit)
    }

    private lateinit var viewModel: WorkoutSessionViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WorkoutSessionViewModel(
            workoutRepository = fakeWorkoutRepository,
            exerciseRepository = fakeExerciseRepository,
            calculateOneRepMaxUseCase = CalculateOneRepMaxUseCase(),
            calculateWorkoutVolumeUseCase = CalculateWorkoutVolumeUseCase()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initSession loads available exercises and initial state`() = runTest(testDispatcher) {
        viewModel.initSession()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.availableExercises.size)
        assertEquals("Press Banca", state.selectedExercise?.name)
    }

    @Test
    fun `adding and removing sets updates state and set numbers`() = runTest(testDispatcher) {
        viewModel.initSession()
        advanceUntilIdle()

        viewModel.onAddSet()
        val state1 = viewModel.uiState.value
        assertEquals(1, state1.sets.size)
        assertEquals(1, state1.sets[0].setNumber)

        viewModel.onAddSet()
        val state2 = viewModel.uiState.value
        assertEquals(2, state2.sets.size)
        assertEquals(2, state2.sets[1].setNumber)

        val firstSetId = state2.sets[0].id
        viewModel.onDeleteSet(firstSetId)
        val state3 = viewModel.uiState.value
        assertEquals(1, state3.sets.size)
    }

    @Test
    fun `updating weight and reps updates volume and estimated 1RM`() = runTest(testDispatcher) {
        viewModel.initSession()
        advanceUntilIdle()

        viewModel.onAddSet()
        val setId = viewModel.uiState.value.sets[0].id

        viewModel.onUpdateSet(setId = setId, weightText = "100", repsText = "5", rir = 0)
        viewModel.onToggleSetCompleted(setId)

        val state = viewModel.uiState.value
        val set = state.sets.first { it.id == setId }
        assertEquals("100", set.weightText)
        assertEquals("5", set.repsText)
        assertTrue(set.isCompleted)
        assertEquals(500.0, state.totalVolumeKg)
        assertTrue(set.estimated1Rm > 100.0)
    }

    @Test
    fun `saving workout marks isSavedSuccess as true`() = runTest(testDispatcher) {
        viewModel.initSession()
        advanceUntilIdle()

        viewModel.onTitleChange("Entrenamiento de Pecho")
        viewModel.onAddSet()
        val setId = viewModel.uiState.value.sets[0].id
        viewModel.onUpdateSet(setId = setId, weightText = "80", repsText = "10", rir = 1)

        viewModel.onSaveWorkout()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSavedSuccess)
        assertFalse(state.isSaving)
        assertEquals(1, fakeWorkoutRepository.savedWorkouts.size)
    }

    @Test
    fun `adding exercise with past history preloads latest weight reps and rir`() = runTest(testDispatcher) {
        // Prepare past workout with historical set
        fakeWorkoutRepository.savedWorkouts.add(
            Workout(
                id = "past-w1",
                title = "Sesión anterior",
                date = kotlin.time.Instant.fromEpochMilliseconds(1700000000000),
                sets = listOf(
                    WorkoutSet(
                        id = "past-s1",
                        workoutId = "past-w1",
                        exerciseId = "e1",
                        setNumber = 1,
                        weightKg = 85.0,
                        reps = 8,
                        rir = 1
                    )
                )
            )
        )

        viewModel.initSession()
        advanceUntilIdle()

        // Add set for e1 (Press Banca which has past history)
        viewModel.onAddSet()
        val state = viewModel.uiState.value
        assertEquals(1, state.sets.size)
        val set = state.sets[0]
        assertEquals("85", set.weightText)
        assertEquals("8", set.repsText)
        assertEquals(1, set.rir)
    }

    @Test
    fun `adding exercise without past history uses default values`() = runTest(testDispatcher) {
        viewModel.initSession()
        advanceUntilIdle()

        // Select e2 (Sentadilla which has NO past history)
        viewModel.onSelectExercise(fakeExercises[1])
        viewModel.onAddSet()

        val state = viewModel.uiState.value
        assertEquals(1, state.sets.size)
        val set = state.sets[0]
        assertEquals("60", set.weightText)
        assertEquals("10", set.repsText)
        assertEquals(2, set.rir)
    }
}
