package com.app.android.presentation.viewmodel.viewModelVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma

// HU36 estados de la pantalla de detalle
sealed interface DetalleProgramaUiState {
    data object Loading : DetalleProgramaUiState

    data class Success(
        val detalle: DetallePrograma,
        val accion: AccionDetalle,
    ) : DetalleProgramaUiState

    data object NoEncontrado : DetalleProgramaUiState

    data class Error(
        val mensaje: String,
    ) : DetalleProgramaUiState
}
