package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.useCases.useCasesPerfiles.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// se borró una doble declaración de LoginUiState


@HiltViewModel
class LoginViewModel
@Inject constructor(
    // El viewmodel sólo recibe casos de uso
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private var loginJob: Job? = null

    fun onEmailChange(email: String) {
        if (!_uiState.value.isLoading) _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        if (!_uiState.value.isLoading) _uiState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        val datos = _uiState.value
        if (datos.isLoading || datos.sesionLista != null) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        loginJob = viewModelScope.launch {
            loginUseCase(datos.email, datos.password).fold(
                onSuccess = { sesion ->
                    _uiState.update { it.copy(password = "", isLoading = false, sesionLista = sesion) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                },
            )
        }
    }

    //
    fun consumirSesion() {
        _uiState.update {
            it.copy(sesionLista = null)
        }
    }

    fun cancelLogin() {
        loginJob?.cancel()
        _uiState.update { it.copy(password = "", isLoading = false, error = null, sesionLista = null) }
    }
}
