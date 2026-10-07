package org.marcosnpereira03.gymtracker.data.repository

import org.marcosnpereira03.gymtracker.data.remote.gemini.*
import org.marcosnpereira03.gymtracker.domain.model.ChatMessage
import org.marcosnpereira03.gymtracker.domain.model.MessageSender
import org.marcosnpereira03.gymtracker.domain.repository.AiCoachRepository

/**
 * Implementación del repositorio del Coach de IA consumiendo la API de Google Gemini.
 */
class AiCoachRepositoryImpl(
    private val geminiApiClient: GeminiApiClient = GeminiApiClient()
) : AiCoachRepository {

    override suspend fun sendMessage(
        userPrompt: String,
        conversationHistory: List<ChatMessage>,
        userDataContext: String,
        apiKey: String?
    ): Result<String> {
        val activeKey = apiKey?.trim()?.ifBlank { null } ?: GeminiConfig.apiKey

        // Memoria conversacional: Tomar hasta los últimos 10 mensajes anteriores
        val recentHistory = conversationHistory
            .filter { !it.isError && it.text.isNotBlank() }
            .takeLast(10)

        val contents = mutableListOf<GeminiContentDto>()

        // Mapear historial previo
        recentHistory.forEach { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            contents.add(
                GeminiContentDto(
                    role = role,
                    parts = listOf(GeminiPartDto(text = msg.text))
                )
            )
        }

        // Agregar el nuevo mensaje del usuario
        contents.add(
            GeminiContentDto(
                role = "user",
                parts = listOf(GeminiPartDto(text = userPrompt))
            )
        )

        val systemInstruction = if (userDataContext.isNotBlank()) {
            GeminiSystemInstructionDto(
                parts = listOf(GeminiPartDto(text = userDataContext))
            )
        } else null

        val request = GeminiRequestDto(
            contents = contents,
            systemInstruction = systemInstruction,
            generationConfig = GeminiGenerationConfigDto(
                temperature = 0.7,
                maxOutputTokens = 1200
            )
        )

        return geminiApiClient.generateContent(
            request = request,
            apiKey = activeKey
        )
    }

    override fun getApiKey(): String = GeminiConfig.apiKey

    override fun setApiKey(apiKey: String) {
        GeminiConfig.apiKey = apiKey.trim()
    }
}
