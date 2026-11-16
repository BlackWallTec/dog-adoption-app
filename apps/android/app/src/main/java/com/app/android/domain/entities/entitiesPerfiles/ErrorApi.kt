package com.app.android.domain.entities.entitiesPerfiles

/**
 * Códigos de error. Los primeros nueve son los de shared/errores.js (ERROR_CODES) y llegan del
 * backend en el campo "code". Los demás son propios de la app y nunca vienen del servidor.
 */
enum class CodigoError {
    // Backend
    VALIDATION_ERROR,
    EMAIL_EXISTS,
    CONFLICT,
    BAD_REQUEST,
    INVALID_CREDENTIALS,
    UNAUTHENTICATED,
    FORBIDDEN,
    RATE_LIMITED,
    INTERNAL_ERROR,

    // App
    SIN_CONEXION,
    RESPUESTA_INVALIDA,
    GUARDADO_SESION,
}

/**
 * Único error de dominio de perfiles.
 * [mensaje] es el "error" del backend (si lo hubo) y [campos] su "fields" (422 y 409).
 */
class ErrorApi(
    val codigo: CodigoError,
    val mensaje: String? = null,
    val campos: Map<String, String> = emptyMap(),
) : Exception(mensaje ?: codigo.name)