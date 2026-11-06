package com.app.android.presentation.viewmodel.viewModelDonativos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.useCases.useCasesDonativos.ErroresDatosFiscales
import com.app.android.domain.useCases.useCasesDonativos.GetDatosFiscalesUseCase
import com.app.android.domain.useCases.useCasesDonativos.GuardarDatosFiscalesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados 1: carga los datos al abrir la pantalla
sealed interface DatosFiscalesUiState {
    data object Loading : DatosFiscalesUiState

    data class Success(
        val datos: DatosFiscales,
        val yaGuardados: Boolean, // false = sale el aviso de "aún no has guardado tus datos"
        val constanciaNueva: ConstanciaArchivo? = null, // la que escogió y todavía no se sube
        val infoConstancia: String = "", // "0.4 MB · Lista para guardar"
        val errores: ErroresDatosFiscales = ErroresDatosFiscales(),
    ) : DatosFiscalesUiState

    data class Error(
        val mensaje: String,
    ) : DatosFiscalesUiState
}

// Maquina de estados 2: guarda al picar "Guardar datos fiscales"
sealed interface GuardarDatosFiscalesUiState {
    data object Idle : GuardarDatosFiscalesUiState

    data object Loading : GuardarDatosFiscalesUiState

    data object Success : GuardarDatosFiscalesUiState // la pantalla avisa que se guardó

    data class Error(
        val mensaje: String,
    ) : GuardarDatosFiscalesUiState
}

@HiltViewModel
class DatosFiscalesViewModel
    @Inject
    constructor(
        private val getDatosFiscalesUseCase: GetDatosFiscalesUseCase,
        private val guardarDatosFiscalesUseCase: GuardarDatosFiscalesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<DatosFiscalesUiState>(DatosFiscalesUiState.Loading)
        val uiState: StateFlow<DatosFiscalesUiState> = _uiState

        private val _guardarState = MutableStateFlow<GuardarDatosFiscalesUiState>(GuardarDatosFiscalesUiState.Idle)
        val guardarState: StateFlow<GuardarDatosFiscalesUiState> = _guardarState

        // Cargar
        fun onLoadDatosFiscales(idUsuario: String) {
            viewModelScope.launch {
                _uiState.value = DatosFiscalesUiState.Loading
                try {
                    val datos = getDatosFiscalesUseCase(idUsuario)
                    _uiState.value = DatosFiscalesUiState.Success(datos = datos, yaGuardados = datos.completos)
                } catch (e: Exception) {
                    _uiState.value = DatosFiscalesUiState.Error("No se pudieron cargar tus datos fiscales.")
                    Log.e("DONATIVOS", "Error al cargar datos fiscales: ${e.message}", e)
                }
            }
        }

        // CUANDO EL USUARIO ESCRIBE: cambia solo su campo y borra solo su error
        private fun editar(cambio: (DatosFiscalesUiState.Success) -> DatosFiscalesUiState.Success) {
            val actual = _uiState.value as? DatosFiscalesUiState.Success ?: return
            _uiState.value = cambio(actual)
        }

        fun onRfcChange(valor: String) =
            editar { it.copy(datos = it.datos.copy(rfc = valor.uppercase()), errores = it.errores.copy(rfc = null)) }

        fun onRazonSocialChange(valor: String) =
            editar { it.copy(datos = it.datos.copy(razonSocial = valor), errores = it.errores.copy(razonSocial = null)) }

        fun onRegimenChange(clave: String) =
            editar { it.copy(datos = it.datos.copy(regimenFiscal = clave), errores = it.errores.copy(regimenFiscal = null)) }

        fun onCodigoPostalChange(valor: String) =
            editar {
                // solo números y máximo 5
                val limpio = valor.filter { c -> c.isDigit() }.take(5)
                it.copy(datos = it.datos.copy(codigoPostal = limpio), errores = it.errores.copy(codigoPostal = null))
            }

        fun onCorreoChange(valor: String) =
            editar {
                it.copy(datos = it.datos.copy(correoFacturacion = valor), errores = it.errores.copy(correoFacturacion = null))
            }

        fun onConstanciaSeleccionada(
            archivo: ConstanciaArchivo,
            info: String,
        ) = editar { it.copy(constanciaNueva = archivo, infoConstancia = info, errores = it.errores.copy(constancia = null)) }

        // La pantalla ya revisó el archivo y no sirve (tipo o peso)
        fun onConstanciaInvalida(mensaje: String) =
            editar { it.copy(constanciaNueva = null, errores = it.errores.copy(constancia = mensaje)) }

        // GUARDAR
        fun onGuardar(idUsuario: String) {
            val actual = _uiState.value as? DatosFiscalesUiState.Success ?: return
            if (_guardarState.value is GuardarDatosFiscalesUiState.Loading) return

            viewModelScope.launch {
                _guardarState.value = GuardarDatosFiscalesUiState.Loading
                try {
                    val errores = guardarDatosFiscalesUseCase(idUsuario, actual.datos, actual.constanciaNueva)
                    if (errores.hayErrores) {
                        // CA-02: se marcan los campos que faltan y no se guarda
                        _uiState.value = actual.copy(errores = errores)
                        _guardarState.value = GuardarDatosFiscalesUiState.Idle
                    } else {
                        // se vuelve a pedir para que se vea lo que quedó en la BD
                        val guardados = getDatosFiscalesUseCase(idUsuario)
                        _uiState.value = DatosFiscalesUiState.Success(datos = guardados, yaGuardados = guardados.completos)
                        _guardarState.value = GuardarDatosFiscalesUiState.Success
                    }
                } catch (e: Exception) {
                    _guardarState.value = GuardarDatosFiscalesUiState.Error("No se pudo guardar. Intenta de nuevo.")
                    Log.e("DONATIVOS", "Error al guardar datos fiscales: ${e.message}", e)
                }
            }
        }

        // La pantalla ya mostró el mensaje
        fun onMensajeMostrado() {
            _guardarState.value = GuardarDatosFiscalesUiState.Idle
        }
    }
