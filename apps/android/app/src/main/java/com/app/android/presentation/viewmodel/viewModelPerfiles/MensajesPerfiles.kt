package com.app.android.presentation.viewmodel.viewModelPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.FalloAutenticacion
import com.app.android.domain.entities.entitiesPerfiles.MotivoFalloAutenticacion

// Convierte los errores de dominio en mensajes para la interfaz.
internal fun Throwable.mensajePerfiles(): String = when (this) {
    is CredencialesInvalidasException ->
        "Correo o contraseña incorrectos."

    is FalloAutenticacion -> when (motivo) {
        MotivoFalloAutenticacion.ACCESO_RESTRINGIDO ->
            "No tienes permiso para acceder."

        MotivoFalloAutenticacion.DEMASIADOS_INTENTOS ->
            "Demasiados intentos. Espera un momento antes de intentar otra vez."

        MotivoFalloAutenticacion.SIN_CONEXION ->
            "No pudimos conectarnos. Revisa tu conexión."

        MotivoFalloAutenticacion.SERVIDOR ->
            "El servidor no pudo completar la solicitud. Inténtalo más tarde."

        MotivoFalloAutenticacion.RESPUESTA_INVALIDA ->
            "No pudimos interpretar la respuesta del servidor."

        MotivoFalloAutenticacion.GUARDADO_SESION ->
            "No pudimos guardar tu sesión. Inténtalo de nuevo."
    }

    else -> "No pudimos completar la solicitud. Inténtalo de nuevo."
}