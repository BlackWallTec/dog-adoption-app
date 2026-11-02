package com.app.android.presentation.viewmodel.viewModelVoluntariado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.conInscripcionConfirmada
import com.app.android.domain.entities.entitiesVoluntariado.validarInscripcion
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class InscripcionViewModel @Inject constructor(
    private val inscribirseProgramaUseCase: InscribirseProgramaUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InscripcionUiState())
    val uiState: StateFlow<InscripcionUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<InscripcionEvento>(Channel.BUFFERED)
    val eventos: Flow<InscripcionEvento> = _eventos.receiveAsFlow()
    
    fun motivoBloqueo(programa: Programa): String? =
        programa.validarInscripcion(Instant.now(clock))?.toMensaje()

    fun inscribirse(programa: Programa) {
        if (_uiState.value.idProgramaEnProceso != null) return // evita doble toque

        viewModelScope.launch {
            _uiState.update { it.copy(idProgramaEnProceso = programa.id) }
            val resultado = inscribirseProgramaUseCase(programa)
            _uiState.update { it.copy(idProgramaEnProceso = null) }

            val evento =
                when (resultado) {
                    is Resultado.Exito ->
                        InscripcionEvento.Exitosa(programa.conInscripcionConfirmada())
                    is Resultado.Error ->
                        InscripcionEvento.Fallida(
                            mensaje = resultado.error.toMensaje(),
                            debeRecargar = resultado.error.requiereRecarga(),
                        )
                }
            _eventos.send(evento)
        }
    }
}