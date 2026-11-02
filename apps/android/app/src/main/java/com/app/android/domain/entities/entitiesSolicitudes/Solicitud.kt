package com.app.android.domain.entities.entitiesSolicitudes

// ENTITY: una solicitud de adopción (el "expediente" que se va llenando paso a paso)
data class Solicitud(
    val id: String,
    val idUsuario: String,
    val idPerro: String,
    val estado: EstadoSolicitud,
    val aceptoRequisitos: Boolean,
)

// Los estados posibles de una solicitud
enum class EstadoSolicitud{
    BORRADOR, //el usuario esta llenandolo (todavia no existe en la BD)
    PENDIENTE_REVISION,
    EN_PERIODO_PRUEBA,
    APROBADA,
    RECHAZADA,
}