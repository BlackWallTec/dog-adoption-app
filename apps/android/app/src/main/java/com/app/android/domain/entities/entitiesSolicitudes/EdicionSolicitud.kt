package com.app.android.domain.entities.entitiesSolicitudes

// Lo que sabe el borrador cuando alguien edita una
// solicitud rechazada. Si esta vacia significa que es
// una solicitud nueva.

class EdicionSolicitud(
    val idSolicitud: String,
    val original: BorradorSolicitud,
    val tieneIneEnServidor: Boolean,
    val tieneDomicilioEnServidor: Boolean,
)

// Lo que regresa el GET de edición

data class SolicitudParaEditar(
    val idSolicitud: String,
    val idPerro: String,
    val ocupacion: String,
    val tuHogar: TuHogar?,
    val compromiso: Compromiso?,
    val tieneIne: Boolean,
    val tieneDomicilio: Boolean,
)
