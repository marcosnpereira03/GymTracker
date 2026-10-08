package org.marcosnpereira03.gymtracker.domain.model

import kotlin.time.Instant

/**
 * Emisor del mensaje en el chat con el Coach.
 */
enum class MessageSender {
    USER,
    COACH
}

/**
 * Modelo de dominio para un mensaje en la conversación con el Coach de IA.
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val sender: MessageSender,
    val timestamp: Instant,
    val isError: Boolean = false
)
