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

            try {
                if (_uiState.value.mode == AuthMode.LOGIN) {
                    val result = kotlinx.coroutines.withTimeoutOrNull(7000L) {
                        authRepository.signIn(email, password)
                    }
                    if (result == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo establecer la conexión. Verifica tu conexión a internet e inténtalo de nuevo."
                            )
                        }
                    } else if (result.isSuccess) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                successMessage = "¡Sesión iniciada con éxito!"
                            )
                        }
                    } else {
                        val errorMsg = mapAuthError(result.exceptionOrNull(), isLogin = true)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                } else {
                    val result = kotlinx.coroutines.withTimeoutOrNull(7000L) {
                        authRepository.signUp(email, password)
                    }
                    if (result == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "No se pudo establecer la conexión. Verifica tu conexión a internet e inténtalo de nuevo."
                            )
                        }
                    } else if (result.isSuccess) {
                        when (val signUpResult = result.getOrThrow()) {
                            is org.marcosnpereira03.gymtracker.domain.repository.SignUpResult.Authenticated -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = true,
                                        successMessage = "¡Cuenta creada y sesión iniciada con éxito!"
                                    )
                                }
                            }
                            is org.marcosnpereira03.gymtracker.domain.repository.SignUpResult.RequiresEmailConfirmation -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isAuthenticated = false,
                                        mode = AuthMode.LOGIN,
                                        successMessage = "¡Cuenta registrada! Te hemos enviado un correo de confirmación a $email. Por favor verifícalo para iniciar sesión."
                                    )
                                }
                            }
                        }
                    } else {
                        val errorMsg = mapAuthError(result.exceptionOrNull(), isLogin = false)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo establecer la conexión. Verifica tu conexión a internet."
                    )
                }
            }
        }
    }

    private fun mapAuthError(throwable: Throwable?, isLogin: Boolean): String {
        val raw = throwable?.message.orEmpty().lowercase()
        return when {
            raw.contains("invalid_credentials") || raw.contains("invalid login credentials") -> {
                "Cuenta / correo no registrado o contraseña incorrecta."
            }
            raw.contains("email_not_confirmed") || raw.contains("email not confirmed") -> {
                "Debes confirmar tu correo electrónico antes de ingresar. Por favor, revisa tu bandeja de entrada."
            }
            raw.contains("user_already_exists") || raw.contains("already registered") || raw.contains("user already exists") -> {
                "Ya existe una cuenta registrada con este correo electrónico."
            }
            raw.contains("over_email_send_rate_limit") || raw.contains("rate limit") || raw.contains("too many requests") -> {
                "Demasiados intentos seguidos. Por favor, espera unos minutos antes de volver a intentar."
            }
            raw.contains("weak_password") || raw.contains("password should be at least") -> {
                "La contraseña debe contener al menos 6 caracteres."
            }
            raw.contains("unable to resolve host") ||
            raw.contains("connectexception") ||
            raw.contains("unknownhost") ||
            raw.contains("unresolved") ||
            raw.contains("network") ||
            raw.contains("timeout") ||
            raw.contains("failed to connect") ||
            raw.contains("no address") ||
            raw.contains("socket") -> {
                "No se pudo establecer la conexión con el servidor. Verifica tu conexión a internet e inténtalo de nuevo."
            }
            else -> {
                if (isLogin) "No se pudo iniciar sesión. Verifica tus datos o inténtalo más tarde."
                else "No se pudo registrar la cuenta. Verifica tus datos o inténtalo más tarde."
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
