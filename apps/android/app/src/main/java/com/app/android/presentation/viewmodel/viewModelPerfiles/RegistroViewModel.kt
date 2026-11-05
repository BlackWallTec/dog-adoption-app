package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.app.android.domain.entities.entitiesPerfiles.RegistroInvalidoException
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.mensajePerfiles
import com.app.android.domain.entities.entitiesPerfiles.validarRegistro
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val datos: Registro = Registro(), val campos: Map<String, String> = emptyMap(),
    val error: String? = null, val isLoading: Boolean = false,
)

@HiltViewModel
class RegistroViewModel @Inject constructor(private val repository: PerfilesRepositoryInt) : ViewModel() {
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()
    private val sessions = Channel<Sesion>(Channel.BUFFERED)
    val authenticatedSessions = sessions.receiveAsFlow()
    private var requestId = 0
    private var job: Job? = null

    fun actualizar(campo: String, valor: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { state ->
            val d = state.datos
            val datos = when (campo) {
                "nombre" -> d.copy(nombre = valor); "apellidos" -> d.copy(apellidos = valor)
                "email" -> d.copy(email = valor); "telefono" -> d.copy(telefono = valor)
                "fechaNacimiento" -> d.copy(fechaNacimiento = valor); "calle" -> d.copy(calle = valor)
                "numeroExterior" -> d.copy(numeroExterior = valor); "codigoPostal" -> d.copy(codigoPostal = valor)
                "passwd" -> d.copy(passwd = valor); "confirmPasswd" -> d.copy(confirmPasswd = valor)
                else -> d
            }
            state.copy(datos = datos, campos = state.campos - campo, error = null)
        }
    }
    fun privacidad(aceptada: Boolean) {
        if (!_uiState.value.isLoading) _uiState.update { it.copy(datos = it.datos.copy(privacidad = aceptada), campos = it.campos - "privacidad", error = null) }
    }
    fun registrar() {
        if (_uiState.value.isLoading) return
        val datos = _uiState.value.datos
        val errores = validarRegistro(datos)
        _uiState.update { it.copy(campos = errores, error = null) }
        if (errores.isNotEmpty()) return
        val request = ++requestId
        _uiState.update { it.copy(isLoading = true) }
        job = viewModelScope.launch {
            try {
                val sesion = repository.registrar(datos)
                if (request != requestId) return@launch
                _uiState.update { it.copy(datos = it.datos.copy(passwd = "", confirmPasswd = "")) }
                sessions.send(sesion)
            } catch (e: CancellationException) { throw e }
            catch (e: RegistroInvalidoException) {
                if (request == requestId) _uiState.update { it.copy(campos = e.campos, error = e.mensajePerfiles()) }
            } catch (e: Exception) {
                if (request == requestId) _uiState.update { it.copy(error = e.mensajePerfiles()) }
            } finally {
                if (request == requestId) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    fun cancelar() {
        requestId++
        job?.cancel()
        _uiState.update { it.copy(datos = it.datos.copy(passwd = "", confirmPasswd = ""), isLoading = false, error = null) }
    }
}
