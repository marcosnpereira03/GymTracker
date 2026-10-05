package org.marcosnpereira03.gymtracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.checkCurrentSession()
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(isAuthenticated = user != null) }
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailText = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordText = password, errorMessage = null) }
    }

    fun onToggleMode() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onSubmit() {
        val email = _uiState.value.emailText.trim()
        val password = _uiState.value.passwordText.trim()

        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Por favor ingresa un correo electrónico válido.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            if (_uiState.value.mode == AuthMode.LOGIN) {
                val result = authRepository.signIn(email, password)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            successMessage = "¡Sesión iniciada con éxito!"
                        )
                    }
                } else {
                    val ex = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ex
                        )
                    }
                }
            } else {
                val result = authRepository.signUp(email, password)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            successMessage = "¡Cuenta creada exitosamente!"
                        )
                    }
                } else {
                    val ex = result.exceptionOrNull()?.message ?: "Error al registrar la cuenta"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ex
                        )
                    }
                }
            }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    emailText = "",
                    passwordText = "",
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }
}
