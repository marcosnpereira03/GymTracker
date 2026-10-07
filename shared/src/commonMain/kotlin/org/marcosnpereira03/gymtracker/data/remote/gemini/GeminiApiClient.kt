package org.marcosnpereira03.gymtracker.data.remote.gemini

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP multiplataforma para interactuar con la API REST de Google Gemini.
 */
class GeminiApiClient(
    private val httpClient: HttpClient = HttpClient()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Envía una solicitud de generación de contenido al modelo Gemini.
     */
    suspend fun generateContent(
        request: GeminiRequestDto,
        apiKey: String = GeminiConfig.apiKey,
        model: String = GeminiConfig.DEFAULT_MODEL
    ): Result<String> = runCatching {
        val activeKey = apiKey.trim().ifBlank { GeminiConfig.apiKey.trim() }
        if (activeKey.isBlank()) {
            throw IllegalStateException("No se ha configurado la API Key de Google Gemini. Por favor, ingrésala en los ajustes del Coach.")
        }

        val url = "${GeminiConfig.BASE_URL}/$model:generateContent?key=$activeKey"
        val requestJson = json.encodeToString(request)

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestJson)
        }

        val responseBody = response.bodyAsText()

        if (!response.status.isSuccess()) {
            val errorResponse = runCatching { json.decodeFromString<GeminiResponseDto>(responseBody) }.getOrNull()
            val errorMsg = errorResponse?.error?.message ?: "Error al consultar Gemini (HTTP ${response.status.value})"
            throw RuntimeException(errorMsg)
        }

        val responseDto = json.decodeFromString<GeminiResponseDto>(responseBody)
        val textResult = responseDto.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("\n")

        if (textResult.isNullOrBlank()) {
            throw IllegalStateException("Gemini devolvió una respuesta vacía.")
        }

        textResult
    }
}
