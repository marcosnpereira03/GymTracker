package org.marcosnpereira03.gymtracker.presentation.coach

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.marcosnpereira03.gymtracker.domain.model.*
import org.marcosnpereira03.gymtracker.domain.repository.*
import org.marcosnpereira03.gymtracker.domain.usecase.BuildAiUserDataContextUseCase
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAiCoachRepository : AiCoachRepository {
        private var storedApiKey: String = "test-api-key"
        var lastPrompt: String? = null
        var lastHistoryCount: Int = 0
        var shouldFail: Boolean = false

        override suspend fun sendMessage(
            userPrompt: String,
            conversationHistory: List<ChatMessage>,
            userDataContext: String,
            apiKey: String?
        ): Result<String> {
            lastPrompt = userPrompt
            lastHistoryCount = conversationHistory.size
            return if (shouldFail) {
                Result.failure(RuntimeException("Error de conexión con Gemini"))
            } else {
                Result.success("Respuesta del Coach: ¡Excelente progreso!")
            }
        }

        override fun getApiKey(): String = storedApiKey
        override fun setApiKey(apiKey: String) {
            this.storedApiKey = apiKey
        }
    }

    private lateinit var fakeRepo: FakeAiCoachRepository
    private lateinit var viewModel: AiCoachViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAiCoachRepository()

        val fakeExerciseRepo = object : ExerciseRepository {
            override suspend fun getExercises(): Result<List<Exercise>> = Result.success(emptyList())
            override suspend fun searchExercises(query: String): Result<List<Exercise>> = Result.success(emptyList())
            override suspend fun getExerciseById(id: String): Result<Exercise> = Result.failure(NoSuchElementException())
            override suspend fun createExercise(exercise: Exercise): Result<Exercise> = Result.success(exercise)
        }
        val fakeWorkoutRepo = object : WorkoutRepository {
            override suspend fun getWorkouts(): Result<List<Workout>> = Result.success(emptyList())
            override suspend fun getWorkoutById(id: String): Result<Workout> = Result.failure(NoSuchElementException())
            override suspend fun saveWorkout(workout: Workout): Result<Workout> = Result.success(workout)
            override suspend fun deleteWorkout(id: String): Result<Unit> = Result.success(Unit)
        }
        val fakeProfileRepo = object : ProfileRepository {
            override suspend fun getBodyWeightLogs(): Result<List<BodyWeightLog>> = Result.success(emptyList())
            override suspend fun saveBodyWeightLog(log: BodyWeightLog): Result<BodyWeightLog> = Result.success(log)
            override suspend fun deleteBodyWeightLog(id: String): Result<Unit> = Result.success(Unit)
        }
        val fakeAuthRepo = object : AuthRepository {
            private val _currentUser = MutableStateFlow<AuthUser?>(AuthUser("u1", "test@test.com", "TestUser"))
            override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

            override suspend fun signIn(email: String, password: String): Result<AuthUser> = Result.success(AuthUser("u1", email, "TestUser"))
            override suspend fun signUp(email: String, password: String): Result<SignUpResult> = Result.success(SignUpResult.Authenticated(AuthUser("u1", email, "TestUser")))
            override suspend fun signOut(): Result<Unit> = Result.success(Unit)
            override suspend fun checkCurrentSession(): AuthUser? = _currentUser.value
            override suspend fun updateProfile(username: String, avatarUrl: String?): Result<AuthUser> = Result.success(AuthUser("u1", "test@test.com", username))
        }

        val buildContextUseCase = BuildAiUserDataContextUseCase(
            exerciseRepository = fakeExerciseRepo,
            workoutRepository = fakeWorkoutRepo,
            profileRepository = fakeProfileRepo,
            authRepository = fakeAuthRepo
        )

        viewModel = AiCoachViewModel(
            aiCoachRepository = fakeRepo,
            buildAiUserDataContextUseCase = buildContextUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializes with greeting message from Coach`() {
        val state = viewModel.uiState.value
        assertEquals(1, state.messages.size)
        assertEquals(MessageSender.COACH, state.messages.first().sender)
        assertEquals("test-api-key", state.apiKey)
    }

    @Test
    fun `sends message and appends user and coach messages to state`() = runTest(testDispatcher) {
        viewModel.onInputTextChanged("¿Cómo mejorar mi press banca?")
        viewModel.sendMessage()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.messages.size) // Initial greeting + user query + coach response
        assertEquals(MessageSender.USER, state.messages[1].sender)
        assertEquals("¿Cómo mejorar mi press banca?", state.messages[1].text)
        assertEquals(MessageSender.COACH, state.messages[2].sender)
        assertEquals("Respuesta del Coach: ¡Excelente progreso!", state.messages[2].text)
        assertEquals(1, fakeRepo.lastHistoryCount) // 1 previous message passed as history
    }

    @Test
    fun `clears chat and resets conversation`() = runTest(testDispatcher) {
        viewModel.onInputTextChanged("Hola")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.messages.size)

        viewModel.clearChat()
        val state = viewModel.uiState.value
        assertEquals(1, state.messages.size)
        assertEquals(MessageSender.COACH, state.messages.first().sender)
    }

    @Test
    fun `updates and saves api key`() {
        viewModel.onApiKeyInputChanged("new-secret-key")
        viewModel.saveApiKey()

        assertEquals("new-secret-key", viewModel.uiState.value.apiKey)
        assertEquals("new-secret-key", fakeRepo.getApiKey())
        assertFalse(viewModel.uiState.value.showApiKeyDialog)
    }
}
