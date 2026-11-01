package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarSolicitudUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados para crear la solicitud
sealed interface RequisitosUiState {
    data object Idle : RequisitosUiState // Pantalla normal, no ss ha picado Continuar

    data object Loading : RequisitosUiState // Creando la solicitud

    data class Success(
        val idSolicitud: String, // Ya se creo → la pantalla navega con este id
    ) : RequisitosUiState

    data class Error(
        // pretty self explanatory mi no?
        val mensaje: String,
    ) : RequisitosUiState
}

@HiltViewModel
class RequisitosViewModel
@Inject
constructor(
    private val iniciarSolicitudUseCase: IniciarSolicitudUseCase,
) : ViewModel() {
    // Estado 1: ¿palomeo el checkbox?
    private val _aceptado = MutableStateFlow(false)
    val aceptado: StateFlow<Boolean> = _aceptado

    // Estado 2: ¿que esta pasando con la solicitud?
    private val _uiState = MutableStateFlow<RequisitosUiState>(RequisitosUiState.Idle)
    val uiState: StateFlow<RequisitosUiState> = _uiState

    fun onAceptadoChange(valor: Boolean) {
        _aceptado.value = valor
    }

    fun onContinuar(
        idUsuario: String,
        idPerro: String,
    ) {
        // Si no acepto o ya esta cargando, no se hace nada
        if (!_aceptado.value || _uiState.value is RequisitosUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = RequisitosUiState.Loading
            try {
                val solicitud: Solicitud = iniciarSolicitudUseCase(idUsuario, idPerro)
                _uiState.value = RequisitosUiState.Success(solicitud.id)
            } catch (e: Exception) {
                _uiState.value =
                    RequisitosUiState.Error("No se pudo iniciar la solicitud. Intenta de nuevo.")
                Log.e("SOLICITUDES", "Error al iniciar solicitud: ${e.message}", e)
            }
        }
    }

    // La pantalla ya navego = se regresa a Idle para que no navegue sola al volver con back
    fun onNavegacionRealizada() {
        _uiState.value = RequisitosUiState.Idle
    }
}