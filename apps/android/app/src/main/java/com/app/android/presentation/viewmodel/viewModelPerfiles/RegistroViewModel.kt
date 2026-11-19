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

/**
 * Estado del formulario: reúne datos capturados, colonias disponibles, errores y señales de carga/éxito.
 * codigoPostal es el número escrito para buscar; datos.idCodigoPostal identifica la colonia elegida.
 */
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
    val requiereConfirmacion: Boolean = false,
    val correoEnviado: Boolean = true,
)

/**
 * Coordina la creación de cuenta desde presentation mediante RegistrarUseCase.
 * Consulta colonias con BuscarCodigosPostalesUseCase y publica sus resultados para la pantalla.
 * Los errores de validación que devuelve el backend se muestran en los campos correspondientes.
 */
// Estas anotaciones permiten que Hilt cree el ViewModel con los casos de uso que necesita.
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
        // update publica el nuevo estado; copy cambia el campo elegido conservando los demás valores.
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

    /** Cambiar el CP descarta la colonia anterior y cancela la consulta que ya no corresponde. */
    private fun actualizarCodigoPostal(valor: String) {
        val cp = valor.filter(Char::isDigit).take(5)
        // ?. llama a cancel sólo si ya existe una búsqueda: la anterior deja de corresponder al CP escrito.
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

    /** Envía los datos al caso de uso, evita envíos duplicados y limpia las contraseñas tras el éxito. */
    fun registrar() {
        val state = _uiState.value
        if (state.isLoading || state.registroExitoso) return
        // Escribir el CP carga opciones; todavía hay que elegir una colonia de esa lista.
        if (state.cargandoColonias || state.datos.idCodigoPostal.isBlank() ||
            state.colonias.none { it.idCodigoPostal == state.datos.idCodigoPostal }) {
            _uiState.update { it.copy(
                campos = it.campos + ("id_codigo_postal" to "Selecciona la colonia de tu dirección."),
                error = null,
            ) }
            return
        }
        _uiState.update { it.copy(isLoading = true, campos = emptyMap(), error = null) }
        // launch inicia una corrutina ligada al ViewModel; al destruirse éste, se cancela su alcance.
        registroJob = viewModelScope.launch {
            // fold separa el Result en éxito (onSuccess) o fallo (onFailure).
            registrar(state.datos).fold(
                onSuccess = { resultado -> _uiState.update { it.copy(
                    isLoading = false, registroExitoso = true,
                    requiereConfirmacion = resultado.requiereConfirmacion,
                    correoEnviado = resultado.correoEnviado,
                    datos = it.datos.copy(contrasena = "", confirmContrasena = ""),
                ) } },
                // as? convierte si el error es ErrorApi; ?. y orEmpty dejan un mapa vacío en otro caso.
                onFailure = { error -> _uiState.update { it.copy(
                    isLoading = false, error = error.message, campos = (error as? ErrorApi)?.campos.orEmpty(),
                ) } },
            )
        }
    }

    // Retira la señal de éxito después de atenderla para que la pantalla no repita la navegación.
    fun consumirRegistro() { _uiState.update { it.copy(registroExitoso = false) } }

    /** Cancela registro y búsqueda de colonias, y retira las contraseñas del estado del formulario. */
    fun cancelar() {
        registroJob?.cancel()
        cpJob?.cancel()
        _uiState.update { it.copy(
            datos = it.datos.copy(contrasena = "", confirmContrasena = ""),
            cargandoColonias = false, isLoading = false, error = null, registroExitoso = false,
        ) }
    }
}
