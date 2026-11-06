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

// Estos estados los tengo que separar luego en un archivo diferente para seguir correctamente la estructura MVVM
// Lo mismo con los exceptions
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

        // El "papelito": true = ya se guardo en el borrador
        private val _listo = MutableStateFlow(false)
        val listo: StateFlow<Boolean> = _listo

        // Lee del borrador, no hay red
        fun onLoadTuHogar() {
            try {
                _uiState.value = TuHogarUiState.Success(datos = getTuHogarUseCase())
            } catch (e: Exception) {
                _uiState.value = TuHogarUiState.Error("No se pudieron cargar tus respuestas.")
                Log.e("SOLICITUDES", "Error al cargar tu hogar: ${e.message}", e)
            }
        }

        // Cuando el usuario cambia cualquier campo
        // La pantalla manda el TuHogar ompleto con el cambio (datos.copy(campo = valor))
        // Se limpian los errores: se vuelven a calcular cuando pide Continuar
        fun onDatosChange(nuevos: TuHogar) {
            val actual = _uiState.value as? TuHogarUiState.Success ?: return
            _uiState.value = actual.copy(datos = nuevos, errores = TuHogarErrors())
        }

        // Valida y escribe en el borrador; si hay errores no navega
        fun onContinuar() {
            val actual = _uiState.value as? TuHogarUiState.Success ?: return
            val errores = guardarTuHogarUseCase(actual.datos)
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
