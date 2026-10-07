package org.marcosnpereira03.gymtracker.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import org.marcosnpereira03.gymtracker.domain.model.*
import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.SignUpResult
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class BuildAiUserDataContextUseCaseTest {

    private class FakeExerciseRepo(val list: List<Exercise>) : ExerciseRepository {
        override suspend fun getExercises(): Result<List<Exercise>> = Result.success(list)
        override suspend fun searchExercises(query: String): Result<List<Exercise>> = Result.success(list)
        override suspend fun getExerciseById(id: String): Result<Exercise> = Result.success(list.first { it.id == id })
        override suspend fun createExercise(exercise: Exercise): Result<Exercise> = Result.success(exercise)
    }

    private class FakeWorkoutRepo(val list: List<Workout>) : WorkoutRepository {
        override suspend fun getWorkouts(): Result<List<Workout>> = Result.success(list)
        override suspend fun getWorkoutById(id: String): Result<Workout> = Result.success(list.first { it.id == id })
        override suspend fun saveWorkout(workout: Workout): Result<Workout> = Result.success(workout)
        override suspend fun deleteWorkout(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeProfileRepo(val weights: List<BodyWeightLog>) : ProfileRepository {
        override suspend fun getBodyWeightLogs(): Result<List<BodyWeightLog>> = Result.success(weights)
        override suspend fun saveBodyWeightLog(log: BodyWeightLog): Result<BodyWeightLog> = Result.success(log)
        override suspend fun deleteBodyWeightLog(id: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeAuthRepo(val user: AuthUser?) : AuthRepository {
        private val _currentUser = MutableStateFlow(user)
        override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

        override suspend fun signIn(email: String, password: String): Result<AuthUser> = Result.success(user!!)
        override suspend fun signUp(email: String, password: String): Result<SignUpResult> = Result.success(SignUpResult.Authenticated(user!!))
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun checkCurrentSession(): AuthUser? = user
        override suspend fun updateProfile(username: String, avatarUrl: String?): Result<AuthUser> = Result.success(user!!)
    }

    @Test
    fun generates_comprehensive_user_context_for_gemini() = kotlinx.coroutines.test.runTest {
        val exercises = listOf(
            Exercise(id = "e1", name = "Press Militar", muscleGroup = "Hombros"),
            Exercise(id = "e2", name = "Sentadilla", muscleGroup = "Piernas")
        )

        val workouts = listOf(
            Workout(
                id = "w1",
                title = "Día de Fuerza",
                date = Instant.parse("2026-10-05T15:00:00Z"),
                notes = "Buena sensación en hombros",
                sets = listOf(
                    WorkoutSet(id = "s1", workoutId = "w1", exerciseId = "e1", setNumber = 1, weightKg = 60.0, reps = 5, rir = 2)
                )
            )
        )

        val weights = listOf(
            BodyWeightLog(id = "bw1", date = Instant.parse("2026-10-05T10:00:00Z"), weightKg = 75.5)
        )

        val user = AuthUser(id = "u1", email = "atleta@test.com", username = "Marcos")

        val useCase = BuildAiUserDataContextUseCase(
            exerciseRepository = FakeExerciseRepo(exercises),
            workoutRepository = FakeWorkoutRepo(workouts),
            profileRepository = FakeProfileRepo(weights),
            authRepository = FakeAuthRepo(user)
        )

        val contextString = useCase()

        assertTrue(contextString.contains("Marcos"), "Debe incluir el nombre del atleta")
        assertTrue(contextString.contains("75.5 kg"), "Debe incluir el peso corporal")
        assertTrue(contextString.contains("Press Militar"), "Debe incluir el catálogo de ejercicios")
        assertTrue(contextString.contains("Día de Fuerza"), "Debe incluir las sesiones recientes")
        assertTrue(contextString.contains("60.0kg x 5 reps (RIR 2)"), "Debe incluir el detalle de las series")
    }
}
