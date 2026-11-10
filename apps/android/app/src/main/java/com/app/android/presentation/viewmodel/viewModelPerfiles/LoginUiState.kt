package com.app.android.presentation.viewmodel.viewModelPerfiles

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val credentialsRejected: Boolean = false,
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
)

/** Validación de interfaz; las reglas de autenticación siguen perteneciendo al servidor. */
internal fun validateLogin(state: LoginUiState): LoginUiState {
    val email = state.email.trim()
    val emailError =
        when {
            email.isBlank() -> "Ingresa tu correo."
            email.length > 254 || !EMAIL_PATTERN.matches(email) -> "Ingresa un correo válido."
            else -> null
        }
    return state.copy(
        emailError = emailError,
        passwordError = if (state.password.isBlank()) "Ingresa tu contraseña." else null,
        error = null,
        credentialsRejected = false,
        isSignedIn = false,
    )
}

private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
