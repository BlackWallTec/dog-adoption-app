package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarBorradorUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ResultadoInicio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados al picar "Continuar" en Requisitos
sealed interface RequisitosUiState {
    data object Idle : RequisitosUiState // Pantalla normal

    data object Loading : RequisitosUiState // Revisando si ya tiene solicitud

    data object Lista : RequisitosUiState // Borrador iniciado → la pantalla navega

    data class Bloqueada(
        val solicitud: SolicitudResumen, // Ya tiene una solicitud activa para este perro
    ) : RequisitosUiState

    data class Error(
        val mensaje: String,
    ) : RequisitosUiState
}

@HiltViewModel
class RequisitosViewModel
@Inject
constructor(
    private val iniciarBorradorUseCase: IniciarBorradorUseCase,
) : ViewModel() {
    // Estado 1: ¿palomeo el checkbox?
    private val _aceptado = MutableStateFlow(false)
    val aceptado: StateFlow<Boolean> = _aceptado

    // Estado 2: ¿que pasa al picar Continuar?
    private val _uiState = MutableStateFlow<RequisitosUiState>(RequisitosUiState.Idle)
    val uiState: StateFlow<RequisitosUiState> = _uiState

    fun onAceptadoChange(valor: Boolean) {
        _aceptado.value = valor
    }

    fun onContinuar(
        idUsuario: String,
        idPerro: String,
    ) {
        if (!_aceptado.value || _uiState.value is RequisitosUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = RequisitosUiState.Loading
            try {
                _uiState.value =
                    when (val resultado = iniciarBorradorUseCase(idUsuario, idPerro)) {
                        is ResultadoInicio.Lista -> RequisitosUiState.Lista
                        is ResultadoInicio.Bloqueada -> RequisitosUiState.Bloqueada(resultado.solicitud)
                    }
            } catch (e: Exception) {
                _uiState.value = RequisitosUiState.Error("No se pudo revisar tu solicitud. Intenta de nuevo.")
                Log.e("SOLICITUDES", "Error al iniciar borrador: ${e.message}", e)
            }
        }
    }

    // La pantalla ya navego: regresamos a Idle para que no navegue sola al volver con back
    fun onNavegacionRealizada() {
        _uiState.value = RequisitosUiState.Idle
    }
}