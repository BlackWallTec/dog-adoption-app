package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.FalloAutenticacion
import com.app.android.domain.entities.entitiesPerfiles.mensajePerfiles
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.useCases.useCasesPerfiles.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // El repositorio guarda la sesión cifrada; la contraseña se limpia de la vista.
    private val sessions = Channel<Sesion>(Channel.BUFFERED)
    val authenticatedSessions = sessions.receiveAsFlow()
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
                _uiState.update { it.copy(password = "", isSignedIn = true) }
                sessions.send(session)
            } catch (error: CancellationException) {
                throw error
            } catch (_: CredencialesInvalidasException) {
                if (request == requestId) {
                    _uiState.update {
                        it.copy(error = "Correo o contraseña incorrectos.", credentialsRejected = true)
                    }
                }
            } catch (error: FalloAutenticacion) {
                if (request == requestId) _uiState.update { it.copy(error = error.mensajePerfiles()) }
            } catch (_: Exception) {
                if (request == requestId) {
                    _uiState.update { it.copy(error = "No pudimos iniciar sesión. Inténtalo más tarde.") }
                }
            } finally {
                if (request == requestId) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun cancelLogin() {
        requestId++
        loginJob?.cancel()
        _uiState.update { it.copy(password = "", isLoading = false, error = null) }
    }
}
