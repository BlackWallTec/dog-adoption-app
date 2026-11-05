package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.useCases.useCasesSolicitudes.CalcularEdadUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ErroresDatosGenerales
import com.app.android.domain.useCases.useCasesSolicitudes.GetDatosGeneralesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarDatosGeneralesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados: CARGAR al abrir (del borrador o del perfil)
sealed interface DatosGeneralesUiState {
    data object Loading : DatosGeneralesUiState

    data class Success(
        val datos: DatosGenerales,
        val edad: Int?,
        val errores: ErroresDatosGenerales = ErroresDatosGenerales(),
    ) : DatosGeneralesUiState

    data class Error(
        val mensaje: String,
    ) : DatosGeneralesUiState
}

@HiltViewModel
class DatosGeneralesViewModel
@Inject
constructor(
    private val getDatosGeneralesUseCase: GetDatosGeneralesUseCase,
    private val guardarDatosGeneralesUseCase: GuardarDatosGeneralesUseCase,
    private val calcularEdadUseCase: CalcularEdadUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DatosGeneralesUiState>(DatosGeneralesUiState.Loading)
    val uiState: StateFlow<DatosGeneralesUiState> = _uiState

    // El "papelito": true = ya se guardo en el borrador, la pantalla debe navegar
    private val _listo = MutableStateFlow(false)
    val listo: StateFlow<Boolean> = _listo

    // ===== CARGAR =====
    fun onLoadDatosGenerales() {
        viewModelScope.launch {
            _uiState.value = DatosGeneralesUiState.Loading
            try {
                val datos = getDatosGeneralesUseCase() // del borrador o del perfil
                _uiState.value =
                    DatosGeneralesUiState.Success(
                        datos = datos,
                        edad = calcularEdadUseCase(datos.fechaNacimiento),
                    )
            } catch (e: Exception) {
                _uiState.value = DatosGeneralesUiState.Error("No se pudieron cargar tus datos.")
                Log.e("SOLICITUDES", "Error al cargar datos generales: ${e.message}", e)
            }
        }
    }

    // ===== EL USUARIO ESCRIBE =====
    fun onOcupacionChange(valor: String) {
        val actual = _uiState.value as? DatosGeneralesUiState.Success ?: return
        _uiState.value =
            actual.copy(
                datos = actual.datos.copy(ocupacion = valor),
                errores = actual.errores.copy(ocupacion = null),
            )
    }

    // ===== GUARDAR (en el borrador, sin red) =====
    fun onContinuar() {
        val actual = _uiState.value as? DatosGeneralesUiState.Success ?: return
        val errores = guardarDatosGeneralesUseCase(actual.datos)
        if (errores.hayErrores) {
            _uiState.value = actual.copy(errores = errores) // se muestran en el campo
        } else {
            _listo.value = true // → la pantalla navega
        }
    }

    // La pantalla ya navego
    fun onNavegacionRealizada() {
        _listo.value = false
    }
}