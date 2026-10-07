package org.marcosnpereira03.gymtracker.data.remote.gemini

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
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
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    companion object {
        fun createDefaultHttpClient(): HttpClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000L
                connectTimeoutMillis = 30_000L
                socketTimeoutMillis = 60_000L
            }
        }
    }

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

        val requestJson = json.encodeToString(request)
        val candidateModels = if (model == GeminiConfig.DEFAULT_MODEL) {
            GeminiConfig.FALLBACK_MODELS
        } else {
            listOf(model) + GeminiConfig.FALLBACK_MODELS.filter { it != model }
        }

        var lastError: Throwable? = null

        for (currentModel in candidateModels) {
            try {
                val url = "${GeminiConfig.BASE_URL}/$currentModel:generateContent?key=$activeKey"
                val response = httpClient.post(url) {
                    headers.append("x-goog-api-key", activeKey)
                    contentType(ContentType.Application.Json)
                    setBody(requestJson)
                }

                val responseBody = response.bodyAsText()

                if (!response.status.isSuccess()) {
                    val errorResponse = runCatching { json.decodeFromString<GeminiResponseDto>(responseBody) }.getOrNull()
                    val errorMsg = errorResponse?.error?.message ?: "Error al consultar Gemini (HTTP ${response.status.value})"
                    // Si es 404, 503 o 500, probamos el siguiente modelo
                    if (response.status.value in listOf(404, 500, 503)) {
                        lastError = RuntimeException(errorMsg)
                        continue
                    }
                    throw RuntimeException(errorMsg)
                }

                val responseDto = json.decodeFromString<GeminiResponseDto>(responseBody)
                val textResult = responseDto.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("\n")

                if (!textResult.isNullOrBlank()) {
                    return@runCatching textResult
                }
            } catch (e: Exception) {
                lastError = e
                // Si aún quedan modelos candidatos, continuamos intentando con el siguiente
                if (candidateModels.last() != currentModel) {
                    continue
                }
                throw e
            }
        }

        throw lastError ?: IllegalStateException("Gemini devolvió una respuesta vacía o los modelos no respondieron a tiempo.")
    }
}


