package com.app.android.domain.entities.entitiesSolicitudes

// ENTITY: lo que la app sabe de una solicitud que ya está en la BD (ya fue enviada).
// Se usamo para el bloqueo ("ya tienes una solicitud para este perro")
// y lo regresa el envio final (boton enviar solicitud).
data class SolicitudResumen(
    val idSolicitud: String,
    val estado: EstadoSolicitud,
)

// Estados posibles de una solicitud enviada.
// Ya no existe BORRADOR: el borrador vive solo en la app (BorradorSolicitud).
enum class EstadoSolicitud {
    PENDIENTE_REVISION,
    EN_PERIODO_PRUEBA,
    APROBADA,
    RECHAZADA,
    DESCONOCIDO, // por si la BD manda un estado que la app no conoce
}