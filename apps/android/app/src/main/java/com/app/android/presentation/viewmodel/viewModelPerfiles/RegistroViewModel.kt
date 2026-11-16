package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.useCases.useCasesPerfiles.BuscarCodigosPostalesUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RegistrarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val datos: Registro = Registro(),
    // Solo para buscar colonias; el registro manda el id de la colonia elegida.
    val codigoPostal: String = "",
    val colonias: List<CodigoPostal> = emptyList(),
    val cargandoColonias: Boolean = false,
    val errorColonias: String? = null,
    val campos: Map<String, String> = emptyMap(),
    val error: String? = null,
    val isLoading: Boolean = false,
    // Sube cada vez que el registro falla con errores; la pantalla lo usa para enfocar el primer campo.
    val versionErrores: Int = 0,
    // Cuando el registro termina con éxito se pone en true.
    // RegistroRoute lo lee, navega, y llama viewModel.consumirRegistro() para limpiarlo.
    val registroExitoso: Boolean = false,
)

// Domain/backend usan snake_case en estas llaves; la pantalla usa camelCase.
private val LLAVES_UI = mapOf(
    "fecha_nacimiento" to "fechaNacimiento",
    "numero_exterior"  to "numeroExterior",
    "numero_interior"  to "numeroInterior",
)

private fun Map<String, String>.aCamposUi() = entries.associate { (k, v) ->
    (LLAVES_UI[k] ?: k) to v
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registrar: RegistrarUseCase,
    private val buscarCodigosPostales: BuscarCodigosPostalesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()

    private var requestId = 0
    private var job: Job? = null
    private var cpJob: Job? = null

    fun actualizar(campo: String, valor: String) {
        if (_uiState.value.isLoading) return
        if (campo == "codigoPostal") {
            actualizarCodigoPostal(valor)
            return
        }
        _uiState.update { state ->
            val d = state.datos
            val datos = when (campo) {
                "nombre"            -> d.copy(nombre = valor)
                "apellidos"         -> d.copy(apellidos = valor)
                "correo"            -> d.copy(correo = valor)
                "telefono"          -> d.copy(telefono = valor)
                "fechaNacimiento"   -> d.copy(fechaNacimiento = valor)
                "calle"             -> d.copy(calle = valor)
                "numeroExterior"    -> d.copy(numeroExterior = valor)
                "numeroInterior"    -> d.copy(numeroInterior = valor)
                "contrasena"        -> d.copy(contrasena = valor)
                "confirmContrasena" -> d.copy(confirmContrasena = valor)
                else                -> d
            }
            state.copy(datos = datos, campos = state.campos - campo, error = null)
        }
    }

    fun privacidad(aceptada: Boolean) {
        if (!_uiState.value.isLoading) {
            _uiState.update {
                it.copy(
                    datos  = it.datos.copy(privacidad = aceptada),
                    campos = it.campos - "privacidad",
                    error  = null,
                )
            }
        }
    }

    fun seleccionarColonia(idCodigoPostal: String) {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                datos  = it.datos.copy(idCodigoPostal = idCodigoPostal),
                campos = it.campos - "id_codigo_postal",
                error  = null,
            )
        }
    }

    private fun actualizarCodigoPostal(valor: String) {
        val cp = valor.filter(Char::isDigit).take(5)
        cpJob?.cancel()
        _uiState.update {
            it.copy(
                codigoPostal   = cp,
                colonias       = emptyList(),
                datos          = it.datos.copy(idCodigoPostal = ""),
                cargandoColonias = cp.length == 5,
                errorColonias  = null,
                campos         = it.campos - "codigoPostal" - "id_codigo_postal",
                error          = null,
            )
        }
        if (cp.length != 5) return

        cpJob = viewModelScope.launch {
            try {
                val colonias = buscarCodigosPostales(cp)
                _uiState.update {
                    it.copy(
                        colonias         = colonias,
                        cargandoColonias = false,
                        errorColonias    = if (colonias.isEmpty()) "No encontramos colonias para ese código postal." else null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargandoColonias = false, errorColonias = e.mensajePerfiles()) }
            }
        }
    }

    fun registrar() {
        if (_uiState.value.isLoading) return
        val datos = _uiState.value.datos
        val request = ++requestId
        _uiState.update { it.copy(isLoading = true, campos = emptyMap(), error = null) }

        job = viewModelScope.launch {
            try {
                registrar(datos)
                if (request != requestId) return@launch
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        datos     = it.datos.copy(contrasena = "", confirmContrasena = ""),
                        registroExitoso = true,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorApi) {
                if (request == requestId) {
                    // RegistrarUseCase valida las reglas del dominio y devuelve los errores por campo.
                    _uiState.update {
                        it.copy(
                            isLoading      = false,
                            campos         = e.campos.aCamposUi(),
                            error          = e.mensajePerfiles(),
                            versionErrores = it.versionErrores + 1,
                        )
                    }
                }
            } catch (e: Exception) {
                if (request == requestId) {
                    _uiState.update { it.copy(isLoading = false, error = e.mensajePerfiles()) }
                }
            }
        }
    }

    // La Route llama esto después de leer registroExitoso y navegar, para limpiar el flag.
    fun consumirRegistro() {
        _uiState.update { it.copy(registroExitoso = false) }
    }

    fun cancelar() {
        requestId++
        job?.cancel()
        cpJob?.cancel()
        _uiState.update {
            it.copy(
                datos            = it.datos.copy(contrasena = "", confirmContrasena = ""),
                cargandoColonias = false,
                isLoading        = false,
                error            = null,
            )
        }
    }
}