package com.app.android.presentation.viewmodel.viewModelVoluntariado

sealed interface ProgramasUiState {
    data object Loading : ProgramasUiState
    data object Empty : ProgramasUiState
    // Data de presentación: la View solo pinta las tarjetas, no conoce la entidad Programa
    data class Success(val programas: List<ProgramaUi>) : ProgramasUiState
    data class Error(val mensaje: String) : ProgramasUiState
}