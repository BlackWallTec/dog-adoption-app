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
import java.security.Guard
import javax.inject.Inject


// Maquina de estados 1: Carga los datos al abrir la pantalla
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

// Maquina de estados 2: GUARDA al picar "Continuar"
sealed interface GuardarUiState {
    data object Idle : GuardarUiState // No se ha picado Continuar

    data object Loading : GuardarUiState // Guardando

    data object Success : GuardarUiState // Guardado = la pantalla navega

    data class Error(
        val mensaje: String,
    ) : GuardarUiState
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

    private val _guardarState = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
    val guardarState: StateFlow<GuardarUiState> = _guardarState

    // Cargar
    fun onLoadDatosGenerales(
        idSolicitud: String,
        idUsuario: String,
    ) {
        viewModelScope.launch {
            _uiState.value = DatosGeneralesUiState.Loading
            try {
                val datos: DatosGenerales = getDatosGeneralesUseCase(idSolicitud, idUsuario)
                _uiState.value =
                    DatosGeneralesUiState.Success(
                        datos = datos,
                        edad = calcularEdadUseCase(datos.fechaNacimiento)
                    )
            } catch (e: Exception) {
                _uiState.value = DatosGeneralesUiState.Error("No se pudieron cargar tus datos.")
                Log.e("SOLICITUDES", "Error al cargar datos generales: ${e.message}", e)
            }
        }
    }

    // CUANDO EL USUARIO ESCRIBE, que solo escribe en ocupacion
    // cambia solo su campo y borra solo su error

    fun onOcupacionChange(valor: String) {
        val actual = _uiState.value as? DatosGeneralesUiState.Success ?: return
        _uiState.value =
            actual.copy(
                datos = actual.datos.copy(ocupacion = valor),
                errores = actual.errores.copy(ocupacion = null),
            )
    }

    // GUARDAR
    fun onContinuar(idSolicitud: String) {
        val actual = _uiState.value as? DatosGeneralesUiState.Success ?: return
        if (_guardarState.value is GuardarUiState.Loading) return

        viewModelScope.launch {
            _guardarState.value = GuardarUiState.Loading
            try {
                val errores = guardarDatosGeneralesUseCase(idSolicitud, actual.datos)
                if (errores.hayErrores) {
                    // Se muestan los errores en los campos y no se navega (notpossible nuhuh)
                    _uiState.value = actual.copy(errores = errores)
                    _guardarState.value = GuardarUiState.Idle
                } else {
                    _guardarState.value = GuardarUiState.Success
                }
            } catch (e: Exception) {
                _guardarState.value = GuardarUiState.Error("No se pudo guardar. Intenta de nuevo.")
                Log.e("SOLICITUDES", "Error al guardar datos generales: ${e.message}", e)
            }
        }
    }

    // La pantalla ya navego
    fun onNavegacionRealizada() {
        _guardarState.value = GuardarUiState.Idle
    }
}
