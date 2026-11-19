package com.app.android.presentation.viewmodel.viewModelVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.ErroresFiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas


sealed interface ProgramasUiState {
    data object Loading : ProgramasUiState
    data object Empty : ProgramasUiState
    // Data de presentación: la View solo pinta las tarjetas, no conoce la entidad Programa
    data class Success(val programas: List<ProgramaUi>) : ProgramasUiState
    data class Error(val mensaje: String) : ProgramasUiState
}

// Información de los controles de filtrado que necesita la pantalla.
data class FiltrosProgramasUiState (
    val seleccionados : FiltrosProgramas = FiltrosProgramas(),
    val aplicados : FiltrosProgramas = FiltrosProgramas(),
    val errores : ErroresFiltrosProgramas = ErroresFiltrosProgramas(),
    val sinCoincidencias : Boolean = false,
)