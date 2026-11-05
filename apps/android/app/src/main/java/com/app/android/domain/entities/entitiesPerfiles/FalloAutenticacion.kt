package com.app.android.domain.entities.entitiesPerfiles

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
