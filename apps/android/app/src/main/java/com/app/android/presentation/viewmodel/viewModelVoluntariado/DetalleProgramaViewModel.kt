package com.app.android.presentation.viewmodel.viewModelVoluntariado

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.entities.entitiesVoluntariado.accion
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerDetalleProgramaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

// HU36 carga el detalle del programa cuyo id llega en la ruta de navegacion
@HiltViewModel
class DetalleProgramaViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val obtenerDetalle: ObtenerDetalleProgramaUseCase,
    ) : ViewModel() {
        // El nombre debe coincidir con el argumento de la ruta (Screen.DetallePrograma)
        private val idPrograma: String = checkNotNull(savedStateHandle[ARG_ID_PROGRAMA])

        private val _uiState = MutableStateFlow<DetalleProgramaUiState>(DetalleProgramaUiState.Loading)
        val uiState: StateFlow<DetalleProgramaUiState> = _uiState.asStateFlow()

        init {
            cargar()
        }

        // Tambien se llama al tocar Reintentar o al volver a la pantalla, para reflejar cupo o cancelacion recientes
        fun cargar() {
            viewModelScope.launch {
                _uiState.value = DetalleProgramaUiState.Loading
                _uiState.value =
                    when (val resultado = obtenerDetalle(idPrograma)) {
                        is Resultado.Exito -> {
                            DetalleProgramaUiState.Success(
                                detalle = resultado.data,
                                accion = resultado.data.accion(Instant.now()),
                            )
                        }

                        is Resultado.Error -> {
                            if (resultado.error == VoluntariadoError.ProgramaNoEncontrado) {
                                DetalleProgramaUiState.NoEncontrado
                            } else {
                                DetalleProgramaUiState.Error(mensajeDe(resultado.error))
                            }
                        }
                    }
            }
        }

        private fun mensajeDe(error: VoluntariadoError): String =
            if (error == VoluntariadoError.SinConexion) {
                "Sin conexión. Revisa tu internet e inténtalo de nuevo."
            } else {
                "No pudimos cargar el programa. Inténtalo de nuevo."
            }

        companion object {
            const val ARG_ID_PROGRAMA = "idPrograma"
        }
    }
