package com.app.android.domain.entities.entitiesSolicitudes

data class SolicitudCliente(
    val idSolicitud: String,
    val folio: String,
    val fechaSolicitud: String,
    val estado: EstadoSolicitud,
    val nombrePerro: String?, // null = el perro ya no está disponible
    val fotoUrl: String?,
    val documentoRechazado: Boolean,
    val comentarioAdmin: String?, // solo el texto pensado para el cliente
) {
    // REGLA: solo se puede editar mientras tenga un documento rechazado
    // (la HU de editar documentos va a usar esta misma regla)
    val puedeEditar: Boolean
        get() = documentoRechazado && estado == EstadoSolicitud.PENDIENTE_REVISION
}
