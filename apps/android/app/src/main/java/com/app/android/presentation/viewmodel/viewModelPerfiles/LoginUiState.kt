package com.app.android.presentation.viewmodel.viewModelPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val credentialsRejected: Boolean = false,
    val isLoading: Boolean = false,
    // Cuando el login termina con éxito, aquí llega la sesión.
    // LoginRoute la lee, navega, y llama viewModel.consumirSesion() para limpiarla.
    val sesionLista: Sesion? = null,
)

/** Validación de interfaz; las reglas de autenticación siguen perteneciendo al servidor. */
internal fun validateLogin(state: LoginUiState): LoginUiState {
    val email = state.email.trim()
    val emailError = when {
        email.isBlank()                                       -> "Ingresa tu correo."
        email.length > 254 || !EMAIL_PATTERN.matches(email)  -> "Ingresa un correo válido."
        else                                                  -> null
    }
    return state.copy(
        emailError         = emailError,
        passwordError      = if (state.password.isBlank()) "Ingresa tu contraseña." else null,
        error              = null,
        credentialsRejected = false,
    )
}

// Gracias IA por otro día en que me das el regex como lo quería
private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")