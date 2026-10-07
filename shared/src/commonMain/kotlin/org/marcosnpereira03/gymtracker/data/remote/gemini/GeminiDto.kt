package org.marcosnpereira03.gymtracker.data.remote.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload de solicitud para la API de Google Gemini (generateContent).
 */
@Serializable
data class GeminiRequestDto(
    @SerialName("contents") val contents: List<GeminiContentDto>,
    @SerialName("system_instruction") val systemInstruction: GeminiSystemInstructionDto? = null,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfigDto? = null
)

@Serializable
data class GeminiSystemInstructionDto(
    @SerialName("parts") val parts: List<GeminiPartDto>
)

@Serializable
data class GeminiContentDto(
    @SerialName("role") val role: String,
    @SerialName("parts") val parts: List<GeminiPartDto>
)

@Serializable
data class GeminiPartDto(
    @SerialName("text") val text: String
)

@Serializable
data class GeminiGenerationConfigDto(
    @SerialName("temperature") val temperature: Double? = 0.7,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int? = 1200
)

/**
 * Respuesta devuelta por la API de Google Gemini.
 */
@Serializable
data class GeminiResponseDto(
    @SerialName("candidates") val candidates: List<GeminiCandidateDto>? = null,
    @SerialName("error") val error: GeminiErrorDto? = null
)

@Serializable
data class GeminiCandidateDto(
    @SerialName("content") val content: GeminiContentDto? = null,
    @SerialName("finishReason") val finishReason: String? = null
)

@Serializable
data class GeminiErrorDto(
    @SerialName("code") val code: Int? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("status") val status: String? = null
)
