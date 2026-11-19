package com.app.android.domain.entities.entitiesSolicitudes

data class SolicitudCliente(
    val idSolicitud: String,
    val folio: String,
    val fechaSolicitud: String,
    val estado: EstadoSolicitud,
    val nombrePerro: String?, // null = el perro ya no está disponible
    val fotoUrl: String?,
    val comentarioAdmin: String?, // solo el texto pensado para el cliente
    val edicionHabilitada: Boolean, // edición que lanza el administrador
) {
    // REGLA: solo se puede editar mientras tenga un documento rechazado
    // (la HU de editar documentos va a usar esta misma regla)
    val puedeEditar: Boolean
        get() = edicionHabilitada && estado == EstadoSolicitud.RECHAZADA
}
