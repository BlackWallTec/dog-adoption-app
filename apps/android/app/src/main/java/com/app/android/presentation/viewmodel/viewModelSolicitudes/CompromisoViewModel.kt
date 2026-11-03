package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.CompromisoErrors
import com.app.android.domain.useCases.useCasesSolicitudes.GetCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarCompromisoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados 1: carga lo que ya habia guardado al abrir la pantalla

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

        private val _guardarState = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
        val guardarState: StateFlow<GuardarUiState> = _guardarState

        // Cargar
        fun onLoadCompromiso(idSolicitud: String) {
            viewModelScope.launch {
                _uiState.value = CompromisoUiState.Loading
                try {
                    val datos = getCompromisoUseCase(idSolicitud)
                    _uiState.value = CompromisoUiState.Success(datos = datos)
                } catch (e: Exception) {
                    _uiState.value = CompromisoUiState.Error("No se pudieron cargar tus respuestas.")
                    Log.e("SOLICITUDES", "Error al cargar compromiso: ${e.message}", e)
                }
            }
        }

        // Cuando el usuario cambia cualquier campo (llega el compromiso completo con el cambio)
        fun onDatosChange(nuevos: Compromiso) {
            val actual = _uiState.value as? CompromisoUiState.Success ?: return
            _uiState.value = actual.copy(datos = nuevos, errores = CompromisoErrors())
        }

        // Guardar (valida + guarda; si hay errores no navega)
        fun onContinuar(idSolicitud: String) {
            val actual = _uiState.value as? CompromisoUiState.Success ?: return
            if (_guardarState.value is GuardarUiState.Loading) return

            viewModelScope.launch {
                _guardarState.value = GuardarUiState.Loading
                try {
                    val errores = guardarCompromisoUseCase(idSolicitud, actual.datos)
                    if (errores.hasErrors) {
                        // Se muestan los errores en los campos y no se navega
                        _uiState.value = actual.copy(errores = errores)
                        _guardarState.value = GuardarUiState.Idle
                    } else {
                        _guardarState.value = GuardarUiState.Success
                    }
                } catch (e: Exception) {
                    _guardarState.value = GuardarUiState.Error("No se pudo guardar. Intenta de nuevo.")
                    Log.e("SOLICITUDES", "Error al guardar compromiso: ${e.message}", e)
                }
            }
        }

        // La pantalla ya navego
        fun onNavegacionRealizada() {
            _guardarState.value = GuardarUiState.Idle
        }
    }
