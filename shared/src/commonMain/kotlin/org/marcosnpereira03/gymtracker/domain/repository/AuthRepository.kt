package org.marcosnpereira03.gymtracker.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.marcosnpereira03.gymtracker.domain.model.AuthUser

/**
 * Contrato de repositorio para gestionar autenticación con Supabase Auth.
 */
interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
    suspend fun checkCurrentSession(): AuthUser?
}
