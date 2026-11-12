package com.app.android.presentation.viewmodel.viewModelSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud

// Lo que la pantalla necesita de cada solicitud para pintar.
data class SolicitudItem(
    val idSolicitud: String,
    val nombrePerro: String, // el ViewModel pone "Mascota no disponible" si el perro ya no está
    val fotoUrl: String?, // null = la pantalla usa la imagen de prueba
    val folio: String,
    val fecha: String, // esta fecha ya esta formateada al pasar por el VM
    val estado: EstadoSolicitud,
    val documentoRechazado: Boolean,
    val puedeEditar: Boolean, // regla de negocio: la decide el ViewModel, no la vista
    val comentarioAdmin: String?, // solo el texto pensado para el cliente
)

// Los estados de la pantalla
sealed interface MisSolicitudesUiState {
    data object Loading : MisSolicitudesUiState

    data class Success(
        val solicitudes: List<SolicitudItem>,
    ) : MisSolicitudesUiState

    data class Error(
        val mensaje: String,
    ) : MisSolicitudesUiState
}
