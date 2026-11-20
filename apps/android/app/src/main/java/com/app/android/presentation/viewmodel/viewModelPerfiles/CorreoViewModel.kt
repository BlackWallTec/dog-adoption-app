package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.useCases.useCasesPerfiles.CorreoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CorreoUiState(
    val correo: String = "", val recuperar: Boolean = false,
    val codigo: String = "", val contrasena: String = "", val repetida: String = "",
    val codigoSolicitado: Boolean = false, val cargando: Boolean = false,
    val mensaje: String? = null, val error: String? = null, val completado: Boolean = false,
)

@HiltViewModel
class CorreoViewModel @Inject constructor(
    private val caso: CorreoUseCase, savedState: SavedStateHandle,
) : ViewModel() {
    private val recuperar = savedState.get<Boolean>("recuperar") ?: false
    private val _state = MutableStateFlow(CorreoUiState(
        correo = savedState.get<String>("correo").orEmpty(), recuperar = recuperar,
        codigoSolicitado = !recuperar,
    ))
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun correo(valor: String) { if (!_state.value.cargando) _state.update { it.copy(correo = valor, error = null) } }
    fun codigo(valor: String) { if (!_state.value.cargando) _state.update { it.copy(codigo = valor.filter(Char::isDigit).take(6), error = null) } }
    fun contrasena(valor: String) { if (!_state.value.cargando) _state.update { it.copy(contrasena = valor, error = null) } }
    fun repetida(valor: String) { if (!_state.value.cargando) _state.update { it.copy(repetida = valor, error = null) } }

    fun solicitar() {
        val s = _state.value
        ejecutar({ caso.solicitar(s.correo, s.recuperar) }) {
            // Respuesta genérica: no afirma que exista una cuenta para el correo escrito.
            it.copy(codigoSolicitado = true, codigo = "", mensaje = "Si la cuenta corresponde, recibirás un código. Espera un minuto antes de solicitar otro.")
        }
    }
    fun confirmar() {
        val s = _state.value
        ejecutar({ if (s.recuperar) caso.restablecer(s.correo, s.codigo, s.contrasena, s.repetida)
            else caso.confirmar(s.correo, s.codigo) }) {
            it.copy(completado = true, codigo = "", contrasena = "", repetida = "")
        }
    }
    private fun ejecutar(operacion: suspend () -> Result<Unit>, exito: (CorreoUiState) -> CorreoUiState) {
        if (_state.value.cargando || _state.value.completado) return
        _state.update { it.copy(cargando = true, error = null, mensaje = null) }
        job = viewModelScope.launch {
            operacion().fold(
                onSuccess = { _state.update { exito(it).copy(cargando = false) } },
                onFailure = { error -> _state.update { it.copy(cargando = false, error = error.message) } },
            )
        }
    }
    // Sólo conserva el correo en navegación. Ni el código ni las contraseñas se guardan en SavedState.
    fun cancelar() { job?.cancel(); _state.update { it.copy(cargando = false, codigo = "", contrasena = "", repetida = "") } }
    fun consumirExito() { _state.update { it.copy(completado = false) } }
}
