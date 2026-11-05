package com.app.android.presentation.viewmodel.viewModelDonativos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.useCases.useCasesDonativos.RegistrarDonativoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DonarDineroUiState {
    data object Idle : DonarDineroUiState

    data object Enviando : DonarDineroUiState

    data class Enviado(
        val monto: String,
    ) : DonarDineroUiState

    data class Error(
        val mensaje: String,
    ) : DonarDineroUiState
}

@HiltViewModel
class DonarDineroViewModel
    @Inject
    constructor(
        private val registrarDonativoUseCase: RegistrarDonativoUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<DonarDineroUiState>(DonarDineroUiState.Idle)
        val uiState: StateFlow<DonarDineroUiState> = _uiState

        fun onEnviar(
            idUsuario: String,
            monto: String,
            referencia: String,
            comprobante: ComprobanteArchivo,
        ) {
            // para que dos toques seguidos no registren dos donativos
            if (_uiState.value is DonarDineroUiState.Enviando) return

            viewModelScope.launch {
                _uiState.value = DonarDineroUiState.Enviando
                try {
                    registrarDonativoUseCase(idUsuario, monto, referencia, comprobante)
                    _uiState.value = DonarDineroUiState.Enviado(monto)
                } catch (e: Exception) {
                    _uiState.value = DonarDineroUiState.Error("No se pudo enviar tu donativo. Intenta de nuevo.")
                    Log.e("DONATIVOS", "Error al registrar el donativo: ${e.message}", e)
                }
            }
        }

        // Ya se navegó a la confirmación
        fun onEnviadoMostrado() {
            _uiState.value = DonarDineroUiState.Idle
        }
    }
