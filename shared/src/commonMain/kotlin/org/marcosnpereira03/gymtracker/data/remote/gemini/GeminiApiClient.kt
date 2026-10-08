package org.marcosnpereira03.gymtracker.data.remote.gemini

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP multiplataforma para interactuar con la API REST de Google Gemini.
 * Incluye reintentos automáticos con retroceso ante sobrecarga de tráfico o límites temporales de tasa.
 */
class GeminiApiClient(
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    companion object {
        const val MAX_RETRIES_PER_MODEL = 3
        const val INITIAL_BACKOFF_MS = 1500L

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
     * Si se detecta saturación de tráfico, error 429/503 o sobrecarga temporal,
     * reintenta automáticamente con retroceso mientras la UI permanece en estado de procesamiento.
     */
    suspend fun generateContent(
        request: GeminiRequestDto,
        apiKey: String = GeminiConfig.apiKey,
        model: String = GeminiConfig.DEFAULT_MODEL
    ): Result<String> = runCatching {
        val activeKey = apiKey.trim().ifBlank { GeminiConfig.apiKey.trim() }
        if (activeKey.isBlank()) {
            throw IllegalStateException("No se ha configurado la API Key de Google Gemini.")
        }

        val requestJson = json.encodeToString(request)
        val candidateModels = if (model == GeminiConfig.DEFAULT_MODEL) {
            GeminiConfig.FALLBACK_MODELS
        } else {
            listOf(model) + GeminiConfig.FALLBACK_MODELS.filter { it != model }
        }

        var lastError: Throwable? = null

        for (currentModel in candidateModels) {
            var attempt = 0
            while (attempt < MAX_RETRIES_PER_MODEL) {
                attempt++
                try {
                    val url = "${GeminiConfig.BASE_URL}/$currentModel:generateContent?key=$activeKey"
                    val response = httpClient.post(url) {
                        headers.append("x-goog-api-key", activeKey)
                        contentType(ContentType.Application.Json)
                        setBody(requestJson)
                    }

                    val responseBody = response.bodyAsText()

                    if (!response.status.isSuccess()) {
                        val statusCode = response.status.value
                        val errorResponse = runCatching { json.decodeFromString<GeminiResponseDto>(responseBody) }.getOrNull()
                        val rawErrorMsg = errorResponse?.error?.message ?: responseBody

                        if (isTransientTrafficOrRateLimitError(statusCode, rawErrorMsg)) {
                            lastError = RuntimeException("Gemini saturado o con límite temporal ($statusCode): $rawErrorMsg")
                            if (attempt < MAX_RETRIES_PER_MODEL) {
                                delay(INITIAL_BACKOFF_MS * attempt)
                                continue
                            }
                            // Si se agotaron los intentos en este modelo, probamos el siguiente modelo de respaldo
                            break
                        }

                        // Si es 404 (modelo no disponible), pasar al siguiente modelo de inmediato
                        if (statusCode == 404) {
                            lastError = RuntimeException("Modelo no encontrado (404): $currentModel")
                            break
                        }

                        // Otro error no recuperable (ej. clave inválida 400/403)
                        throw RuntimeException("Error en API de Gemini: $statusCode")
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
                    } else {
                        lastError = RuntimeException("Respuesta vacía del modelo $currentModel")
                    }
                } catch (e: Exception) {
                    lastError = e
                    if (isNetworkOrTransientException(e) && attempt < MAX_RETRIES_PER_MODEL) {
                        delay(INITIAL_BACKOFF_MS * attempt)
                        continue
                    }
                    break
                }
            }
        }

        throw lastError ?: IllegalStateException("No se pudo obtener respuesta del Coach de IA.")
    }

    private fun isTransientTrafficOrRateLimitError(statusCode: Int, errorMessage: String): Boolean {
        if (statusCode in listOf(429, 500, 502, 503, 504, 529)) return true
        val lower = errorMessage.lowercase()
        return lower.contains("resource_exhausted") ||
                lower.contains("quota") ||
                lower.contains("rate limit") ||
                lower.contains("too many requests") ||
                lower.contains("overloaded") ||
                lower.contains("unavailable") ||
                lower.contains("high traffic") ||
                lower.contains("capacity") ||
                lower.contains("try again")
    }

    private fun isNetworkOrTransientException(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("timeout") ||
                msg.contains("connect") ||
                msg.contains("socket") ||
                msg.contains("traffic") ||
                msg.contains("overload") ||
                msg.contains("resource_exhausted") ||
                msg.contains("rate limit")
    }
}


