package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.useCases.useCasesSolicitudes.GetTuHogarUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarTuHogarUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.TuHogarErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados 1: carga lo que ya habia guardado al abrir la pantalla

sealed interface TuHogarUiState {
    data object Loading : TuHogarUiState

    data class Success(
        val datos: TuHogar,
        val errores: TuHogarErrors = TuHogarErrors(),
    ) : TuHogarUiState

    data class Error(
        val mensaje: String,
    ) : TuHogarUiState
}

// La maquina de estados 2 (guardar) es GuardarUiState (idle/loading/success/error)
@HiltViewModel
class TuHogarViewModel
    @Inject
    constructor(
        private val getTuHogarUseCase: GetTuHogarUseCase,
        private val guardarTuHogarUseCase: GuardarTuHogarUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<TuHogarUiState>(TuHogarUiState.Loading)
        val uiState: StateFlow<TuHogarUiState> = _uiState

        private val _guardarState = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
        val guardarState: StateFlow<GuardarUiState> = _guardarState

        // Cargar
        fun onLoadTuHogar(idSolicitud: String) {
            viewModelScope.launch {
                _uiState.value = TuHogarUiState.Loading
                try {
                    val datos = getTuHogarUseCase(idSolicitud)
                    _uiState.value = TuHogarUiState.Success(datos = datos)
                } catch (e: Exception) {
                    _uiState.value = TuHogarUiState.Error("No se pudieron cargar tus respuestas.")
                    Log.e("SOLICITUDES", "Error al cargar tu hogar: ${e.message}", e)
                }
            }
        }

        // Cuando el usuario cambia cualquier campo
        // La pantalla manda el TuHogar ompleto con el cambio (datos.copy(campo = valor))
        // Se limpian los errores: se vuelven a calcular cuando pide Continuar
        fun onDatosChange(nuevos: TuHogar) {
            val actual = _uiState.value as? TuHogarUiState.Success ?: return
            _uiState.value = actual.copy(datos = nuevos, errores = TuHogarErrors())
        }

        // Guardar (valida + guarda; si hay errores no navega)
        fun onContinuar(idSolicitud: String) {
            val actual = _uiState.value as? TuHogarUiState.Success ?: return
            if (_guardarState.value is GuardarUiState.Loading) return
            viewModelScope.launch {
                _guardarState.value = GuardarUiState.Loading
                try {
                    val errores = guardarTuHogarUseCase(idSolicitud, actual.datos)
                    if (errores.hasErrors) {
                        // Se muestan los errores en los campos y no se navega
                        _uiState.value = actual.copy(errores = errores)
                        _guardarState.value = GuardarUiState.Idle
                    } else {
                        _guardarState.value = GuardarUiState.Success
                    }
                } catch (e: Exception) {
                    _guardarState.value = GuardarUiState.Error("No se pudo guardar. Intenta de nuevo.")
                    Log.e("SOLICITUDES", "Error al guardar tu hogar: ${e.message}", e)
                }
            }
        }

        // La pantalla ya navego
        fun onNavegacionRealizada() {
            _guardarState.value = GuardarUiState.Idle
        }
    }
