package com.app.android.presentation.viewmodel.viewModelVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa

sealed interface ProgramasUiState {
    data object Loading : ProgramasUiState
    data object Empty : ProgramasUiState
    data class Success(val programas: List<Programa>) : ProgramasUiState
    data class Error(val mensaje: String) : ProgramasUiState
}