package org.marcosnpereira03.gymtracker.presentation.coach

import org.marcosnpereira03.gymtracker.domain.model.ChatMessage

/**
 * Estado inmutable de la pantalla del Coach de IA.
 */
data class AiCoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null,
    val apiKey: String = "",
    val apiKeyInput: String = "",
    val showApiKeyDialog: Boolean = false,
    val suggestions: List<String> = listOf(
        "¿Cómo va mi progreso en las últimas semanas?",
        "Analiza el volumen de mis entrenamientos recientes",
        "¿Qué grupo muscular me convendría entrenar hoy?",
        "¿Cómo puedo mejorar mi sobrecarga progresiva?"
    )
)
