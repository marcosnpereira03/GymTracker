package org.marcosnpereira03.gymtracker.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.marcosnpereira03.gymtracker.domain.model.AuthUser

sealed class SignUpResult {
    data class Authenticated(val user: AuthUser) : SignUpResult()
    data class RequiresEmailConfirmation(val email: String) : SignUpResult()
}

/**
 * Contrato de repositorio para gestionar autenticación con Supabase Auth.
 */
interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String): Result<SignUpResult>
    suspend fun signOut(): Result<Unit>
    suspend fun checkCurrentSession(): AuthUser?
    suspend fun updateProfile(username: String, avatarUrl: String? = null): Result<AuthUser>
}

