package org.marcosnpereira03.gymtracker.domain.repository

import org.marcosnpereira03.gymtracker.domain.model.ChatMessage

/**
 * Contrato de acceso al servicio del Coach de Inteligencia Artificial.
 */
interface AiCoachRepository {
    /**
     * Envía un mensaje al Coach de IA con memoria contextual (últimos 10 mensajes) y datos del usuario.
     */
    suspend fun sendMessage(
        userPrompt: String,
        conversationHistory: List<ChatMessage>,
        userDataContext: String,
        apiKey: String? = null
    ): Result<String>

    /**
     * Obtiene el historial completo de mensajes de la conversación actual.
     */
    fun getConversationHistory(): List<ChatMessage>

    /**
     * Guarda o actualiza el historial de mensajes de la conversación.
     */
    fun saveConversationHistory(messages: List<ChatMessage>)

    /**
     * Limpia el historial de la conversación.
     */
    fun clearConversationHistory()

    /**
     * Obtiene la clave de API activa configurada.
     */
    fun getApiKey(): String

    /**
     * Establece o actualiza la clave de API de Gemini.
     */
    fun setApiKey(apiKey: String)
}
