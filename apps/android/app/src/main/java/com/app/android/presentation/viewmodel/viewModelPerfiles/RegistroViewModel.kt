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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val datos: Registro = Registro(),
    val codigoPostal: String = "",
    val colonias: List<CodigoPostal> = emptyList(),
    val cargandoColonias: Boolean = false,
    val errorColonias: String? = null,
    val campos: Map<String, String> = emptyMap(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false,
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registrar: RegistrarUseCase,
    private val buscarCodigosPostales: BuscarCodigosPostalesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()
    private var registroJob: Job? = null
    private var cpJob: Job? = null

    fun actualizar(campo: String, valor: String) {
        if (_uiState.value.isLoading) return
        if (campo == "codigoPostal") { actualizarCodigoPostal(valor); return }
        _uiState.update { state ->
            val d = state.datos
            val datos = when (campo) {
                "nombre" -> d.copy(nombre = valor)
                "apellidos" -> d.copy(apellidos = valor)
                "correo" -> d.copy(correo = valor)
                "telefono" -> d.copy(telefono = valor)
                "fecha_nacimiento" -> d.copy(fechaNacimiento = valor)
                "calle" -> d.copy(calle = valor)
                "numero_exterior" -> d.copy(numeroExterior = valor)
                "numero_interior" -> d.copy(numeroInterior = valor)
                "contrasena" -> d.copy(contrasena = valor)
                "confirmContrasena" -> d.copy(confirmContrasena = valor)
                else -> d
            }
            state.copy(datos = datos, campos = state.campos - campo, error = null)
        }
    }

    fun privacidad(aceptada: Boolean) {
        if (!_uiState.value.isLoading) _uiState.update {
            it.copy(datos = it.datos.copy(privacidad = aceptada), campos = it.campos - "privacidad", error = null)
        }
    }

    fun seleccionarColonia(id: String) {
        if (_uiState.value.isLoading || _uiState.value.colonias.none { it.idCodigoPostal == id }) return
        _uiState.update { it.copy(datos = it.datos.copy(idCodigoPostal = id), campos = it.campos - "id_codigo_postal", error = null) }
    }

    private fun actualizarCodigoPostal(valor: String) {
        val cp = valor.filter(Char::isDigit).take(5)
        cpJob?.cancel()
        _uiState.update { it.copy(
            codigoPostal = cp, colonias = emptyList(), datos = it.datos.copy(idCodigoPostal = ""),
            cargandoColonias = cp.length == 5, errorColonias = null,
            campos = it.campos - "id_codigo_postal", error = null,
        ) }
        if (cp.length != 5) return
        cpJob = viewModelScope.launch {
            buscarCodigosPostales(cp).fold(
                onSuccess = { colonias -> _uiState.update { it.copy(
                    colonias = colonias, cargandoColonias = false,
                    errorColonias = if (colonias.isEmpty()) "No encontramos colonias para ese código postal." else null,
                ) } },
                onFailure = { error -> _uiState.update { it.copy(cargandoColonias = false, errorColonias = error.message) } },
            )
        }
    }

    fun registrar() {
        val state = _uiState.value
        if (state.isLoading || state.registroExitoso) return
        _uiState.update { it.copy(isLoading = true, campos = emptyMap(), error = null) }
        registroJob = viewModelScope.launch {
            registrar(state.datos).fold(
                onSuccess = { _uiState.update { it.copy(
                    isLoading = false, registroExitoso = true,
                    datos = it.datos.copy(contrasena = "", confirmContrasena = ""),
                ) } },
                onFailure = { error -> _uiState.update { it.copy(
                    isLoading = false, error = error.message, campos = (error as? ErrorApi)?.campos.orEmpty(),
                ) } },
            )
        }
    }

    fun consumirRegistro() { _uiState.update { it.copy(registroExitoso = false) } }

    fun cancelar() {
        registroJob?.cancel()
        cpJob?.cancel()
        _uiState.update { it.copy(
            datos = it.datos.copy(contrasena = "", confirmContrasena = ""),
            cargandoColonias = false, isLoading = false, error = null, registroExitoso = false,
        ) }
    }
}
