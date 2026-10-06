package org.marcosnpereira03.gymtracker.domain.model

/**
 * Representa al usuario autenticado en la aplicación.
 */
data class AuthUser(
    val id: String,
    val email: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null
)

