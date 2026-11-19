package com.app.android.presentation.viewmodel.viewModelVoluntariado

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerMisProgramasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class MisProgramasViewModel @Inject constructor(
    private val obtenerMisProgramas: ObtenerMisProgramasUseCase,
    private val cancelarInscripcionEnPrograma: CancelarInscripcionUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgramasUiState>(ProgramasUiState.Loading)
    val uiState: StateFlow<ProgramasUiState> = _uiState.asStateFlow()

    // Mensajes de una sola vez (snackbar)
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private var programas: List<Programa> = emptyList()

    // Programa con una cancelación en curso: su botón muestra "Cancelando…" y se bloquea el doble toque
    private var idProcesando: String? = null

    init {
        cargarProgramas()
    }

    fun cargarProgramas() {
        viewModelScope.launch {
            _uiState.value = ProgramasUiState.Loading
            try {
                programas = obtenerMisProgramas()
                publicarProgramas()
            } catch (e: Exception) {
                Log.e("HU40", "Error loading my programs", e)
                _uiState.value = ProgramasUiState.Error("No se pudieron cargar tus voluntariados")
            }
        }
    }

    fun cancelarInscripcion(idPrograma: String) {
        if (idProcesando != null) return
        val programa = programas.find { it.id == idPrograma } ?: return
        idProcesando = idPrograma
        publicarProgramas()

        viewModelScope.launch {
            try {
                when (val resultado = cancelarInscripcionEnPrograma(programa)) {
                    is Resultado.Exito -> {
                        // Ya no está inscrito: la tarjeta sale de la lista
                        quitar(idPrograma)
                        _mensajes.emit("Cancelaste tu inscripción en ${programa.nombre}.")
                    }
                    is Resultado.Error -> {
                        // Si ya no estaba inscrito, tampoco debe aparecer aquí
                        if (resultado.error == VoluntariadoError.NoInscrito) quitar(idPrograma)
                        _mensajes.emit(mensajeDe(resultado.error))
                    }
                }
            } finally {
                idProcesando = null
                publicarProgramas()
            }
        }
    }

    private fun quitar(idPrograma: String) {
        programas = programas.filterNot { it.id == idPrograma }
    }

    // Entities -> data de presentación. Todas las tarjetas se evalúan con la misma hora.
    private fun publicarProgramas() {
        val ahora = Instant.now(clock)
        _uiState.value =
            if (programas.isEmpty()) {
                ProgramasUiState.Empty
            } else {
                ProgramasUiState.Success(
                    programas.map { programa ->
                        val ui = programa.toUi(ahora)
                        if (programa.id == idProcesando) {
                            ui.copy(textoBoton = "Cancelando…", botonHabilitado = false)
                        } else {
                            ui
                        }
                    },
                )
            }
    }

    // Solo errores posibles al cancelar
    private fun mensajeDe(error: VoluntariadoError): String =
        when (error) {
            VoluntariadoError.ProgramaIniciado -> "Este programa ya comenzó, ya no es posible cancelar."
            VoluntariadoError.NoInscrito -> "No tienes una inscripción activa en este programa."
            VoluntariadoError.ProgramaNoEncontrado -> "No encontramos este programa. Actualiza la lista."
            VoluntariadoError.NoAutenticado -> "Inicia sesión para continuar."
            VoluntariadoError.SinConexion -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            else -> "Ocurrió un error. Inténtalo de nuevo."
        }
}