package com.app.android.presentation.viewmodel.viewModelVoluntariado

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesVoluntariado.ErroresFiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.ResultadoFiltrado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionConfirmada
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionCancelada
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.FiltrarProgramasUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerProgramasVigentesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
class ProgramasViewModel @Inject constructor(
    private val obtenerProgramasVigentes: ObtenerProgramasVigentesUseCase,
    private val inscribirseEnPrograma: InscribirseProgramaUseCase,
    private val cancelarInscripcionEnPrograma: CancelarInscripcionUseCase,
    private val clock: Clock,
    private val filtrarProgramas: FiltrarProgramasUseCase,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ProgramasUiState>(ProgramasUiState.Loading)
    val uiState: StateFlow<ProgramasUiState> = _uiState.asStateFlow()

    // Mensajes de inscripción y cancelación.
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    // Programa recién inscrito: lo lee la pantalla de confirmación
    private val _programaConfirmado =
        MutableStateFlow<ProgramaUi?>(null)
    val programaConfirmado: StateFlow<ProgramaUi?> =
        _programaConfirmado.asStateFlow()

    private val _inscripcionExitosa =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val inscripcionExitosa: SharedFlow<Unit> =
        _inscripcionExitosa.asSharedFlow()

    private val _cancelacionExitosa =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cancelacionExitosa: SharedFlow<Unit> =
        _cancelacionExitosa.asSharedFlow()

    private val _filtrosState = MutableStateFlow(FiltrosProgramasUiState())
    val filtroState: StateFlow<FiltrosProgramasUiState> =
        _filtrosState.asStateFlow()


    // Entidades del dominio: las necesita el caso de uso; la View nunca las ve
    private var programas: List<Programa> = emptyList()

    // Programa con una inscripción o cancelación en curso: su botón muestra el texto de
    // "procesando" y bloquea el doble toque (en cualquier tarjeta) hasta que termine
    private var idProcesando: String? = null
    private var textoProcesando: String = ""

    init {
        cargarProgramas()
    }

    fun cargarProgramas() {
        viewModelScope.launch {
            _uiState.value = ProgramasUiState.Loading
            try {
                programas = obtenerProgramasVigentes()
                _filtrosState.value = FiltrosProgramasUiState()
                publicarProgramas()
            } catch (e: Exception) {
                Log.e("HU39A", "Error loading programs", e)
                _uiState.value =
                    ProgramasUiState.Error("No se pudieron cargar los programas")
            }
        }
    }

    // HU40: al volver de "Mis Voluntariados" (donde se pudo cancelar)
    fun actualizarProgramas() {
        if (_uiState.value !is ProgramasUiState.Success) return
        viewModelScope.launch {
            recargarSinIndicador()
            publicarProgramas()
        }
    }

    fun inscribirse(idPrograma: String) {
        val programa = iniciarProceso(idPrograma, "Inscribiendo…") ?: return

        viewModelScope.launch {
            try {
                when (val resultado = inscribirseEnPrograma(programa)) {
                    is Resultado.Exito -> {
                        // Descuenta el lugar y marca "Inscrito" sin volver a pedir la lista
                        val confirmado = programa.conInscripcionConfirmada()
                        reemplazar(confirmado)
                        _programaConfirmado.value =
                            confirmado.toUi(Instant.now(clock))
                        _inscripcionExitosa.emit(Unit)
                    }
                    is Resultado.Error -> {
                        when (resultado.error) {
                            // Ya estaba inscrito: la tarjeta también debe reflejarlo
                            VoluntariadoError.YaInscrito -> reemplazar(programa.copy(usuarioInscrito = true))
                            // Alguien ocupó el último lugar: se trae el cupo real del servidor
                            VoluntariadoError.SinCupo -> recargarSinIndicador()
                            else -> Unit
                        }
                        _mensajes.emit(mensajeDe(resultado.error))
                    }
                }
            } finally {
                terminarProceso()
            }
        }
    }

    // HU37: cancela la inscripción (el backend cambia el estado de la fila a "Pendiente")
    fun cancelarInscripcion(idPrograma: String) {
        val programa = iniciarProceso(idPrograma, "Cancelando…") ?: return

        viewModelScope.launch {
            try {
                when (val resultado = cancelarInscripcionEnPrograma(programa)) {
                    is Resultado.Exito -> {
                        // Libera el lugar y regresa el botón a "Inscribirme" sin volver a pedir la lista
                        reemplazar(programa.conInscripcionCancelada())
                        _mensajes.emit("Cancelaste tu inscripción en ${programa.nombre}.")
                        _cancelacionExitosa.emit(Unit)
                    }
                    is Resultado.Error -> {
                        // Ya no estaba inscrito: se trae el estado y el cupo reales del servidor
                        if (resultado.error == VoluntariadoError.NoInscrito) {
                            recargarSinIndicador()
                        }
                        _mensajes.emit(mensajeCancelacionDe(resultado.error))
                    }
                }
            } finally {
                terminarProceso()
            }
        }
    }

    // Marca el programa como "en proceso". Regresa null si ya hay otro en curso o no existe.
    private fun iniciarProceso(idPrograma: String, texto: String): Programa? {
        if (idProcesando != null) return null
        val programa = programas.find { it.id == idPrograma } ?: return null
        idProcesando = idPrograma
        textoProcesando = texto
        publicarProgramas()
        return programa
    }

    private fun terminarProceso() {
        idProcesando = null
        publicarProgramas()
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
                            ui.copy(textoBoton = textoProcesando, botonHabilitado = false)
                        } else {
                            ui
                        }
                    },
                )
            }
    }

    private fun reemplazar(actualizado: Programa) {
        programas = programas.map { if (it.id == actualizado.id) actualizado else it }
    }

    // Recarga sin mostrar el indicador de carga: la lista sigue visible mientras llega el dato real.
    // Si falla, se queda la lista actual (el snackbar ya avisa del problema).
    private suspend fun recargarSinIndicador() {
        try {
            programas = obtenerProgramasVigentes()
        } catch (e: Exception) {
            Log.e("HU39A", "Error refreshing programs", e)
        }
    }

    // Actualiza los controles sin cambiar todavía los resultados.
    fun actualizarFiltros(filtros: FiltrosProgramas) {
        _filtrosState.value =
            _filtrosState.value.copy(
                seleccionados = filtros,
                errores = ErroresFiltrosProgramas(),
            )
    }

    fun aplicarFiltros() {
        if (!puedeFiltrar()) return

        publicarProgramas(
            filtros = _filtrosState.value.seleccionados,
            actualizarValidacion = true,
        )
    }

    fun limpiarFiltros() {
        if (!puedeFiltrar()) return

        _filtrosState.value = FiltrosProgramasUiState()
        publicarProgramas()
    }

    private fun puedeFiltrar(): Boolean =
        _uiState.value is ProgramasUiState.Success ||
                _uiState.value is ProgramasUiState.Empty

    // Al inscribir o cancelar, vuelve a aplicar los filtros ya confirmados.
    private fun publicarProgramas(
        filtros: FiltrosProgramas = _filtrosState.value.aplicados,
        actualizarValidacion: Boolean = false,
    ) {
        when (val resultado = filtrarProgramas(programas, filtros)) {
            is ResultadoFiltrado.Invalido -> {
                _filtrosState.value =
                    _filtrosState.value.copy(
                        errores = resultado.errores,
                    )
                // Conserva los resultados anteriores.
            }

            is ResultadoFiltrado.Exito -> {
                val catalogo = filtrarProgramas(programas, FiltrosProgramas())
                val catalogoVacio =
                    catalogo is ResultadoFiltrado.Exito &&
                            catalogo.programas.isEmpty()

                val estadoFiltros = _filtrosState.value
                _filtrosState.value =
                    estadoFiltros.copy(
                        aplicados = filtros,
                        errores =
                            if (actualizarValidacion) {
                                ErroresFiltrosProgramas()
                            } else {
                                estadoFiltros.errores
                            },
                        sinCoincidencias =
                            !catalogoVacio && resultado.programas.isEmpty(),
                    )

                val ahora = Instant.now(clock)

                _uiState.value =
                    if (catalogoVacio) {
                        ProgramasUiState.Empty
                    } else {
                        ProgramasUiState.Success(
                            resultado.programas.map { programa ->
                                val ui = programa.toUi(ahora)
                                if (programa.id == idProcesando) {
                                    ui.copy(
                                        textoBoton = textoProcesando,
                                        botonHabilitado = false,
                                    )
                                } else {
                                    ui
                                }
                            },
                        )
                    }
            }
        }
    }

    private fun mensajeDe(error: VoluntariadoError): String =
        when (error) {
            VoluntariadoError.SinCupo ->
                "Este programa ya no tiene lugares disponibles."
            VoluntariadoError.YaInscrito ->
                "Ya estás inscrito en este programa."
            VoluntariadoError.NoInscrito ->
                "No tienes una inscripción activa en este programa."
            VoluntariadoError.ProgramaIniciado ->
                "Este programa ya comenzó, ya no es posible inscribirse."
            VoluntariadoError.ProgramaNoDisponible ->
                "Este programa no está disponible por ahora."
            VoluntariadoError.ProgramaNoEncontrado ->
                "No encontramos este programa. Actualiza la lista."
            VoluntariadoError.NoAutenticado ->
                "Inicia sesión para inscribirte."
            VoluntariadoError.SinConexion ->
                "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            VoluntariadoError.Desconocido ->
                "Ocurrió un error. Inténtalo de nuevo."
        }

    private fun mensajeCancelacionDe(error: VoluntariadoError): String =
        if (error == VoluntariadoError.ProgramaIniciado) {
            "Este programa ya comenzó, ya no es posible cancelar."
        } else {
            mensajeDe(error)
        }
}