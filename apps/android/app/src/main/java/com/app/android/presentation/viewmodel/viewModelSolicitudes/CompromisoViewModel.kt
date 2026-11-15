package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.CompromisoErrors
import com.app.android.domain.useCases.useCasesSolicitudes.GetCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarCompromisoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// Estado SOLO de la pantalla: no es dato de la solicitud (no va al borrador ni a la BD).
data class Acceptances(
    val acceptsReturn: Boolean = false,
    val petSpending: Boolean = false,
    val petFollowing: Boolean = false,
) {
    val allAccepted: Boolean get() = acceptsReturn && petSpending && petFollowing
}

// Maquina de estados: carga lo que ya habia guardado al abrir la pantalla

// Estos estados los tengo que separar luego en un archivo diferente para seguir correctamente la estructura MVVM
// Lo mismo con los exceptions
sealed interface CompromisoUiState {
    data object Loading : CompromisoUiState

    data class Success(
        val datos: Compromiso,
        val errores: CompromisoErrors = CompromisoErrors(),
    ) : CompromisoUiState

    data class Error(
        val mensaje: String,
    ) : CompromisoUiState
}

// Para guardar se reutiliza GuardarUiState (Idle/ Loading / Success / Error)

@HiltViewModel
class CompromisoViewModel
    @Inject
    constructor(
        private val getCompromisoUseCase: GetCompromisoUseCase,
        private val guardarCompromisoUseCase: GuardarCompromisoUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<CompromisoUiState>(CompromisoUiState.Loading)
        val uiState: StateFlow<CompromisoUiState> = _uiState

        private val _acceptances = MutableStateFlow(Acceptances())
        val acceptances: StateFlow<Acceptances> = _acceptances

        private val _listo = MutableStateFlow(false)
        val listo: StateFlow<Boolean> = _listo

        // Cargar del borrador sin red
        fun onLoadCompromiso() {
            try {
                _uiState.value = CompromisoUiState.Success(datos = getCompromisoUseCase())
            } catch (e: Exception) {
                _uiState.value = CompromisoUiState.Error("No se pudieron cargar tus respuestas.")
                Log.e("SOLICITUDES", "Error al cargar compromiso: ${e.message}", e)
            }
        }

        // Cuando el usuario cambia cualquier campo (llega el compromiso completo con el cambio)
        fun onDatosChange(nuevos: Compromiso) {
            val actual = _uiState.value as? CompromisoUiState.Success ?: return
            _uiState.value = actual.copy(datos = nuevos, errores = CompromisoErrors())
        }

        fun onAcceptancesChange(nuevos: Acceptances) {
            _acceptances.value = nuevos
        }

        // Guardar (valida + guarda; si hay errores no navega)
        fun onContinuar() {
            val actual = _uiState.value as? CompromisoUiState.Success ?: return
            if (!_acceptances.value.allAccepted) return // el boton esta desactivado
            val errores = guardarCompromisoUseCase(actual.datos)
            if (errores.hasErrors) {
                // Se muestan los errores en los campos y no se navega
                _uiState.value = actual.copy(errores = errores)
            } else {
                _listo.value = true
            }
        }

        // La pantalla ya navego
        fun onNavegacionRealizada() {
            _listo.value = false
        }
    }
