package org.marcosnpereira03.gymtracker.presentation.exercises

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.Workout
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateOneRepMaxUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.GetExerciseHistoryUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeExerciseList = mutableListOf(
        Exercise(id = "e1", name = "Press Banca Plano", muscleGroup = "Pecho"),
        Exercise(id = "e2", name = "Aperturas en Polea", muscleGroup = "Pecho"),
        Exercise(id = "e3", name = "Sentadilla Libre", muscleGroup = "Cuádriceps"),
        Exercise(id = "e4", name = "Dominadas", muscleGroup = "Espalda")
    )

    private val fakeExerciseRepository = object : ExerciseRepository {
        override suspend fun getExercises(): Result<List<Exercise>> = Result.success(fakeExerciseList.toList())
        override suspend fun searchExercises(query: String): Result<List<Exercise>> = Result.success(fakeExerciseList.toList())
        override suspend fun getExerciseById(id: String): Result<Exercise> = Result.success(fakeExerciseList.first { it.id == id })
        override suspend fun createExercise(exercise: Exercise): Result<Exercise> {
            fakeExerciseList.add(exercise)
            return Result.success(exercise)
        }
        override suspend fun updateExercise(exercise: Exercise): Result<Exercise> {
            val index = fakeExerciseList.indexOfFirst { it.id == exercise.id }
            if (index != -1) {
                fakeExerciseList[index] = exercise
            }
            return Result.success(exercise)
        }
        override suspend fun deleteExercise(id: String): Result<Unit> {
            fakeExerciseList.removeAll { it.id == id }
            return Result.success(Unit)
        }
    }

    private val fakeWorkoutRepository = object : WorkoutRepository {
        override suspend fun getWorkouts(): Result<List<Workout>> = Result.success(emptyList())
        override suspend fun getWorkoutById(id: String): Result<Workout> = Result.failure(NoSuchElementException())
        override suspend fun saveWorkout(workout: Workout): Result<Workout> = Result.success(workout)
        override suspend fun deleteWorkout(id: String): Result<Unit> = Result.success(Unit)
    }

    private lateinit var viewModel: ExercisesViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeExerciseList.clear()
        fakeExerciseList.addAll(
            listOf(
                Exercise(id = "e1", name = "Press Banca Plano", muscleGroup = "Pecho"),
                Exercise(id = "e2", name = "Aperturas en Polea", muscleGroup = "Pecho"),
                Exercise(id = "e3", name = "Sentadilla Libre", muscleGroup = "Cuádriceps"),
                Exercise(id = "e4", name = "Dominadas", muscleGroup = "Espalda")
            )
        )
        viewModel = ExercisesViewModel(
            exerciseRepository = fakeExerciseRepository,
            workoutRepository = fakeWorkoutRepository,
            getExerciseHistoryUseCase = GetExerciseHistoryUseCase(CalculateOneRepMaxUseCase())
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads exercises and populates filtered list initially`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(4, state.exercises.size)
        assertEquals(4, state.filteredExercises.size)
    }

    @Test
    fun `filters exercises by muscle group`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onSelectMuscleGroup("Pecho")
        val state = viewModel.uiState.value
        assertEquals("Pecho", state.selectedMuscleGroup)
        assertEquals(2, state.filteredExercises.size)
        assertEquals("Aperturas en Polea", state.filteredExercises[0].exercise.name)
        assertEquals("Press Banca Plano", state.filteredExercises[1].exercise.name)
    }

    @Test
    fun `filters exercises by search query`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Sentadilla")
        val state = viewModel.uiState.value
        assertEquals("Sentadilla", state.searchQuery)
        assertEquals(1, state.filteredExercises.size)
        assertEquals("Sentadilla Libre", state.filteredExercises[0].exercise.name)
    }

    @Test
    fun `updates existing exercise successfully`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val updated = Exercise(id = "e1", name = "Press Banca Inclinado", muscleGroup = "Pecho")
        viewModel.onUpdateExercise(updated)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val ex1 = state.exercises.find { it.exercise.id == "e1" }
        assertEquals("Press Banca Inclinado", ex1?.exercise?.name)
    }

    @Test
    fun `creates custom exercise with equipment successfully`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onCreateCustomExercise(
            name = "Hip Thrust con Barra",
            muscleGroup = "Glúteos",
            equipment = "Barra"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.exercises.size)
        val created = state.exercises.find { it.exercise.name == "Hip Thrust con Barra" }
        assertEquals("Glúteos", created?.exercise?.muscleGroup)
        assertEquals("Barra", created?.exercise?.equipment)
    }

    @Test
    fun `deletes exercise successfully`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onDeleteExercise("e4")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.exercises.size)
        assertEquals(null, state.exercises.find { it.exercise.id == "e4" })
    }
}
