package org.marcosnpereira03.gymtracker.presentation.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.model.ChatMessage
import org.marcosnpereira03.gymtracker.domain.model.MessageSender
import org.marcosnpereira03.gymtracker.domain.repository.AiCoachRepository
import org.marcosnpereira03.gymtracker.domain.usecase.BuildAiUserDataContextUseCase
import org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil
import kotlin.random.Random

/**
 * ViewModel que gestiona el estado y la comunicación con el Coach de IA de Google Gemini.
 * Recupera y persiste los mensajes en el repositorio para mantener la conversación activa
 * incluso al salir y volver a entrar a la pantalla.
 */
class AiCoachViewModel(
    private val aiCoachRepository: AiCoachRepository,
    private val buildAiUserDataContextUseCase: BuildAiUserDataContextUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    init {
        val currentKey = aiCoachRepository.getApiKey()
        val existingHistory = aiCoachRepository.getConversationHistory()

        val initialMessages = if (existingHistory.isNotEmpty()) {
            existingHistory
        } else {
            val greeting = listOf(
                ChatMessage(
                    id = generateId(),
                    text = "¡Hola! Soy tu Coach de Inteligencia Artificial. Tengo acceso a tu catálogo de ejercicios y a tus entrenamientos recientes. ¿En qué puedo ayudarte hoy?",
                    sender = MessageSender.COACH,
                    timestamp = DateTimeUtil.now()
                )
            )
            aiCoachRepository.saveConversationHistory(greeting)
            greeting
        }

        _uiState.update {
            it.copy(
                apiKey = currentKey,
                apiKeyInput = currentKey,
                messages = initialMessages
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun onApiKeyInputChanged(key: String) {
        _uiState.update { it.copy(apiKeyInput = key) }
    }

    fun showApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show, apiKeyInput = it.apiKey) }
    }

    fun saveApiKey() {
        val newKey = _uiState.value.apiKeyInput.trim()
        aiCoachRepository.setApiKey(newKey)
        _uiState.update {
            it.copy(
                apiKey = newKey,
                showApiKeyDialog = false,
                errorMessage = null
            )
        }
    }

    fun clearChat() {
        val resetMessages = listOf(
            ChatMessage(
                id = generateId(),
                text = "Conversación reiniciada. ¿Qué consulta o análisis te gustaría realizar?",
                sender = MessageSender.COACH,
                timestamp = DateTimeUtil.now()
            )
        )
        aiCoachRepository.saveConversationHistory(resetMessages)
        _uiState.update {
            it.copy(
                messages = resetMessages,
                errorMessage = null
            )
        }
    }

    fun sendMessage(promptOverride: String? = null) {
        val query = (promptOverride ?: _uiState.value.inputText).trim()
        if (query.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            id = generateId(),
            text = query,
            sender = MessageSender.USER,
            timestamp = DateTimeUtil.now()
        )

        val updatedMessagesWithUser = _uiState.value.messages + userMessage
        aiCoachRepository.saveConversationHistory(updatedMessagesWithUser)

        // Limpiar input y agregar mensaje del usuario
        _uiState.update { state ->
            state.copy(
                inputText = "",
                messages = updatedMessagesWithUser,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                // 1. Obtener contexto en tiempo real con datos de Supabase
                val userDataContext = buildAiUserDataContextUseCase()

                // 2. Enviar a Gemini incluyendo la memoria conversacional previa (últimos 10 mensajes)
                val history = _uiState.value.messages.dropLast(1)
                val result = aiCoachRepository.sendMessage(
                    userPrompt = query,
                    conversationHistory = history,
                    userDataContext = userDataContext,
                    apiKey = _uiState.value.apiKey
                )

                result.fold(
                    onSuccess = { responseText ->
                        val coachMessage = ChatMessage(
                            id = generateId(),
                            text = responseText,
                            sender = MessageSender.COACH,
                            timestamp = DateTimeUtil.now()
                        )
                        val finalMessages = _uiState.value.messages + coachMessage
                        aiCoachRepository.saveConversationHistory(finalMessages)

                        _uiState.update { state ->
                            state.copy(
                                messages = finalMessages,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = {
                        val friendlyError = USER_FRIENDLY_ERROR_MESSAGE
                        val errorCoachMessage = ChatMessage(
                            id = generateId(),
                            text = friendlyError,
                            sender = MessageSender.COACH,
                            timestamp = DateTimeUtil.now(),
                            isError = true
                        )
                        val finalMessages = _uiState.value.messages + errorCoachMessage
                        aiCoachRepository.saveConversationHistory(finalMessages)

                        _uiState.update { state ->
                            state.copy(
                                messages = finalMessages,
                                isLoading = false,
                                errorMessage = friendlyError
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                val friendlyError = USER_FRIENDLY_ERROR_MESSAGE
                val errorCoachMessage = ChatMessage(
                    id = generateId(),
                    text = friendlyError,
                    sender = MessageSender.COACH,
                    timestamp = DateTimeUtil.now(),
                    isError = true
                )
                val finalMessages = _uiState.value.messages + errorCoachMessage
                aiCoachRepository.saveConversationHistory(finalMessages)

                _uiState.update { state ->
                    state.copy(
                        messages = finalMessages,
                        isLoading = false,
                        errorMessage = friendlyError
                    )
                }
            }
        }
    }

    private fun generateId(): String {
        return "${DateTimeUtil.now().toEpochMilliseconds()}_${Random.nextInt(1000, 9999)}"
    }

    companion object {
        const val USER_FRIENDLY_ERROR_MESSAGE = "Ocurrió un error al procesar la petición. Por favor, intenta de nuevo."
    }
}
