package com.app.android.domain.entities.entitiesPerfiles


// la enum class nos ayuda a poder dentifica el motivo del fallo y
// la excepción permite comunicarlo al ViewModel.
enum class MotivoFalloAutenticacion {
    ACCESO_RESTRINGIDO,
    DEMASIADOS_INTENTOS,
    SIN_CONEXION,
    SERVIDOR,
    RESPUESTA_INVALIDA,
    GUARDADO_SESION,
}

class FalloAutenticacion(
    val motivo: MotivoFalloAutenticacion,
) : Exception("No se pudo completar la autenticación: $motivo")