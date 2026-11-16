package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.useCases.useCasesPerfiles.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var loginJob: Job? = null
    private var requestId = 0

    fun onEmailChange(email: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(email = email, emailError = null, error = null, credentialsRejected = false)
        }
    }

    fun onPasswordChange(password: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(password = password, passwordError = null, error = null, credentialsRejected = false)
        }
    }

    fun login() {
        if (_uiState.value.isLoading) return
        val validated = validateLogin(_uiState.value)
        _uiState.value = validated
        if (validated.emailError != null || validated.passwordError != null) return

        val request = ++requestId
        _uiState.update { it.copy(isLoading = true) }

        loginJob = viewModelScope.launch {
            try {
                val session = loginUseCase(validated.email, validated.password)
                if (request != requestId) return@launch
                // Guardamos la sesión en el estado para que LoginRoute la consuma y navegue.
                _uiState.update { it.copy(password = "", isLoading = false, sesionLista = session) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ErrorApi) {
                if (request == requestId) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.mensajePerfiles(),
                            credentialsRejected = error.codigo == CodigoError.INVALID_CREDENTIALS,
                        )
                    }
                }
            } catch (_: Exception) {
                if (request == requestId) {
                    _uiState.update { it.copy(isLoading = false, error = "No pudimos iniciar sesión. Inténtalo más tarde.") }
                }
            }
        }
    }

    // La Route llama esto después de leer sesionLista y navegar, para limpiar el evento.
    fun consumirSesion() {
        _uiState.update { it.copy(sesionLista = null) }
    }

    fun cancelLogin() {
        requestId++
        loginJob?.cancel()
        _uiState.update { it.copy(password = "", isLoading = false, error = null) }
    }
}