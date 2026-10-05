package org.marcosnpereira03.gymtracker.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.model.AuthUser
import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository

import org.marcosnpereira03.gymtracker.domain.repository.SignUpResult

/**
 * Implementación de AuthRepository utilizando supabase-kt Auth.
 */
class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = supabaseClient.auth.currentUserOrNull()
                        if (user != null) {
                            _currentUser.value = AuthUser(id = user.id, email = user.email)
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _currentUser.value = null
                    }
                    else -> Unit
                }
            }
        }
    }

    override suspend fun checkCurrentSession(): AuthUser? {
        return runCatching {
            val user = supabaseClient.auth.currentUserOrNull()
            val session = supabaseClient.auth.currentSessionOrNull()
            if (session != null && user != null) {
                val authUser = AuthUser(id = user.id, email = user.email)
                _currentUser.value = authUser
                authUser
            } else {
                _currentUser.value = null
                null
            }
        }.getOrNull()
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("No se pudo obtener la sesión del usuario.")
            val authUser = AuthUser(id = user.id, email = user.email)
            _currentUser.value = authUser
            authUser
        }
    }

    override suspend fun signUp(email: String, password: String): Result<SignUpResult> {
        return runCatching {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            val session = supabaseClient.auth.currentSessionOrNull()
            val user = supabaseClient.auth.currentUserOrNull()
            if (session != null && user != null) {
                val authUser = AuthUser(id = user.id, email = user.email)
                _currentUser.value = authUser
                SignUpResult.Authenticated(authUser)
            } else {
                _currentUser.value = null
                SignUpResult.RequiresEmailConfirmation(email.trim())
            }
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            supabaseClient.auth.signOut()
            _currentUser.value = null
        }
    }
}
