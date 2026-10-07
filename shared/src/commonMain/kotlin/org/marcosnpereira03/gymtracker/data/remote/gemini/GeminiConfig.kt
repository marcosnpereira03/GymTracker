package org.marcosnpereira03.gymtracker.data.remote.gemini

import org.marcosnpereira03.gymtracker.config.AppConfig

/**
 * Configuración global del cliente de Google Gemini.
 * Obtiene la clave de forma segura desde AppConfig (generado a partir de local.properties / variables de entorno).
 */
object GeminiConfig {
    const val DEFAULT_MODEL = "gemini-1.5-flash"
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    
    /**
     * Clave de API de Gemini en tiempo de ejecución (inicializada desde AppConfig seguro).
     */
    var apiKey: String = AppConfig.GEMINI_API_KEY
}
