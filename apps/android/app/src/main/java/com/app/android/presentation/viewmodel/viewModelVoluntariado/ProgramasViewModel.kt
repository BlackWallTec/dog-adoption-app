package com.app.android.presentation.viewmodel.viewModelVoluntariado

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerProgramasVigentesUseCase
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionConfirmada
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProgramasViewModel @Inject constructor(
    private val obtenerProgramasVigentes: ObtenerProgramasVigentesUseCase,
    private val inscribirseEnPrograma: InscribirseProgramaUseCase,
    private val cancelarInscripcionEnPrograma: CancelarInscripcionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgramasUiState>(ProgramasUiState.Loading)
    val uiState: StateFlow<ProgramasUiState> = _uiState.asStateFlow()

    // Mensajes de una sola vez (snackbar): errores al inscribirse o cancelar, y aviso de cancelación
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    // Programa recién inscrito: lo lee la pantalla de confirmación
    private val _programaConfirmado = MutableStateFlow<Programa?>(null)
    val programaConfirmado: StateFlow<Programa?> = _programaConfirmado.asStateFlow()

    // Evento de una sola vez: la inscripción salió bien, hay que ir a la confirmación
    private val _inscripcionExitosa = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val inscripcionExitosa: SharedFlow<Unit> = _inscripcionExitosa.asSharedFlow()

    // Evento de una sola vez: la cancelación salió bien (la confirmación lo usa para regresar a la lista)
    private val _cancelacionExitosa = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cancelacionExitosa: SharedFlow<Unit> = _cancelacionExitosa.asSharedFlow()

    // Evita doble toque mientras una inscripción o cancelación sigue en curso
    private var procesando = false

    init {
        cargarProgramas()
    }

    fun cargarProgramas() {
        viewModelScope.launch {
            _uiState.value = ProgramasUiState.Loading
            _uiState.value = try {
                val programas = obtenerProgramasVigentes()
                if (programas.isEmpty()) ProgramasUiState.Empty
                else ProgramasUiState.Success(programas)
            } catch (e: Exception) {
                Log.e("HU39A", "Error loading programs", e)
                ProgramasUiState.Error("No se pudieron cargar los programas")
            }
        }
    }

    fun inscribirse(programa: Programa) {
        if (procesando) return
        procesando = true
        viewModelScope.launch {
            try {
                when (val resultado = inscribirseEnPrograma(programa)) {
                    is Resultado.Exito -> {
                        marcarInscrito(programa.id, true)
                        _programaConfirmado.value = programa.conInscripcionConfirmada()
                        _inscripcionExitosa.emit(Unit)
                    }
                    is Resultado.Error -> {
                        // Si ya estaba inscrito, la tarjeta también debe reflejarlo
                        if (resultado.error == VoluntariadoError.YaInscrito) marcarInscrito(programa.id, true)
                        _mensajes.emit(mensajeDe(resultado.error))
                    }
                }
            } finally {
                procesando = false
            }
        }
    }

    // HU37: cancela la inscripción (el backend cambia el estado de la fila a "Pendiente")
    fun cancelarInscripcion(programa: Programa) {
        if (procesando) return
        procesando = true
        viewModelScope.launch {
            try {
                when (val resultado = cancelarInscripcionEnPrograma(programa)) {
                    is Resultado.Exito -> {
                        marcarInscrito(programa.id, false)
                        _mensajes.emit("Cancelaste tu inscripción en ${programa.nombre}.")
                        _cancelacionExitosa.emit(Unit)
                    }
                    is Resultado.Error -> {
                        // Si ya no estaba inscrito, la tarjeta también debe reflejarlo
                        if (resultado.error == VoluntariadoError.NoInscrito) marcarInscrito(programa.id, false)
                        _mensajes.emit(mensajeCancelacionDe(resultado.error))
                    }
                }
            } finally {
                procesando = false
            }
        }
    }

    private fun marcarInscrito(idPrograma: String, inscrito: Boolean) {
        val actual = _uiState.value
        if (actual is ProgramasUiState.Success) {
            _uiState.value =
                ProgramasUiState.Success(
                    actual.programas.map { if (it.id == idPrograma) it.copy(usuarioInscrito = inscrito) else it },
                )
        }
    }

    private fun mensajeDe(error: VoluntariadoError): String =
        when (error) {
            VoluntariadoError.SinCupo -> "Este programa ya no tiene lugares disponibles."
            VoluntariadoError.YaInscrito -> "Ya estás inscrito en este programa."
            VoluntariadoError.NoInscrito -> "No tienes una inscripción activa en este programa."
            VoluntariadoError.ProgramaIniciado -> "Este programa ya comenzó, ya no es posible inscribirse."
            VoluntariadoError.ProgramaNoDisponible -> "Este programa no está disponible por ahora."
            VoluntariadoError.ProgramaNoEncontrado -> "No encontramos este programa. Actualiza la lista."
            VoluntariadoError.NoAutenticado -> "Inicia sesión para inscribirte."
            VoluntariadoError.SinConexion -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            VoluntariadoError.Desconocido -> "Ocurrió un error. Inténtalo de nuevo."
        }

    // Igual que mensajeDe, salvo el programa ya iniciado (aquí no se puede cancelar)
    private fun mensajeCancelacionDe(error: VoluntariadoError): String =
        if (error == VoluntariadoError.ProgramaIniciado) {
            "Este programa ya comenzó, ya no es posible cancelar."
        } else {
            mensajeDe(error)
        }
}