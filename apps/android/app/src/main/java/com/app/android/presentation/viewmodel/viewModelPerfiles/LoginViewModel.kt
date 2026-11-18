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


// Puede inyectarse con hiltViewModel porque usé @HiltViewModel y
// en LoginScreen.kt, usé viewModel: LoginViewModel = hiltViewModel(),
@HiltViewModel
class LoginViewModel
@Inject constructor(
    // El viewmodel sólo recibe casos de uso
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    // Iniciando MutableStateFlow con un valor inicial de LoginUiState(),
    // que será observado y actualizado conforme a cambios
    // No quieres que tu _uiState quede expuesto más allá del viewmodel
    private val _uiState = MutableStateFlow(LoginUiState())
    // Por eso se representa como un StateFlow READONLY
    val uiState = _uiState.asStateFlow()

    // Recordando la corrutina a cargo de la acción de login
    private var loginJob: Job? = null

    // Métodos para actualizar el estado del viewmodel
    fun onEmailChange(email: String) {
        // Si no está cargando, actualiza el email pasándose el estado a sí mismo a la lambda
        if (!_uiState.value.isLoading)
            // La función especificada para update, es la lambda que refiere al método
            // copy del estado mismo
            _uiState.update {
                it.copy(email = email, error = null)
            }
    }

    fun onPasswordChange(password: String) {
        // Si no está cargando, actualiza password pasándose el estado a sí mismo a la lambda
        if (!_uiState.value.isLoading)
             // La función especificada para update, es la lambda que refiere al método
             // copy del estado mismo
            _uiState.update {
                it.copy(password = password, error = null)
            }
    }

    fun login() {
        // Obtiene la estructura LoginUiState, con los valores observados hasta el momento
        val datos = _uiState.value

        // Si ya está cargando o no hay sesión
        if (datos.isLoading || datos.sesionLista != null) return

        // Establece que está cargando
        // La función especificada para update, es la lambda que refiere
        // al método copy del estado mismo
        _uiState.update {
            it.copy(isLoading = true, error = null)
        }

        // Esto ya utiliza el useCase, puede ser bloqueante porque involucra una solicitud
        // de red. Por eso se utiliza launch para lanzarlo en un hilo distinto del principal
        // como una corrutina.
        loginJob = viewModelScope.launch {
            loginUseCase(datos.email, datos.password).fold(
                // Si el login resulta exitoso, sesion contiene la sesión
                onSuccess = { sesion ->
                    _uiState.update {
                        it.copy(password = "", isLoading = false, sesionLista = sesion)
                    }
                },
                // Si loginUseCase retorna un error
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                },
            )
        }
    }

    // Limpia la sesión
    fun consumirSesion() {
        _uiState.update {
            it.copy(sesionLista = null)
        }
    }

    // Cancela la corrutina de login
    fun cancelLogin() {
        // Como se recomienda en la documentación de CoroutineScope, cancela
        // toda ejecución, por parte de la corrutina ahora que no se necesita hacer un Login
        loginJob?.cancel()
        _uiState.update {
            it.copy(password = "", isLoading = false, error = null, sesionLista = null)
        }
    }
}

/*
Hay un hilo principal para la interfaz de usuario.

Una corrutina es una función que puede ser pausada en determinados puntos y
reanudada posteriormente, sin bloquear el hilo en el que se encuentra

Una corrutina, que al ser lanzada hereda un dispatcher por defecto,
puede reanudarse en otro hilo según el dispatcher.

Al suspenderse una corrutina, deja libre el hilo y puede reanudarse después.
De cara a la reanudación, su estado necesario queda guardado en memoria
(dónde iba la ejecución, variables locales que necesitará después de la suspensión, etc)

Las corrutinas de viewModelScope se ejecutan en el dispatcher principal, por defecto.
Si hay una operación bloqueante, esta debe configurarse para ejecutarse en otro hilo,
distinto del principal.

Los callbacks son funciones llamadas por eventos, como el click de un botón.

Las corrutinas son tareas que pueden suspenderse y ser reanudadas posteriormente.

Los Jobs son objetos que referencian a las corrutinas, y permiten controlar su ejecución,
saber si sigue activa, esperar a que termine, etc.

El StateFlow es un estado observable, no un hilo ni una corrutina.
*/