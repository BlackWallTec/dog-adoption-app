package com.app.android.presentation.viewmodel.viewModelPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi

// Convierte los errores en mensajes para la interfaz. El texto lo decide la app según el código;
// el "error" del backend no se muestra directo (algunos son largos o de otro tono).
internal fun Throwable.mensajePerfiles(): String = when (this) {
    is ErrorApi -> when (codigo) {
        CodigoError.INVALID_CREDENTIALS -> "Correo o contraseña incorrectos."
        CodigoError.UNAUTHENTICATED -> "Tu sesión venció. Inicia sesión de nuevo."
        CodigoError.VALIDATION_ERROR, CodigoError.BAD_REQUEST -> "Revisa los datos del formulario."
        CodigoError.EMAIL_EXISTS, CodigoError.CONFLICT -> "Ya existe una cuenta con ese correo."
        CodigoError.FORBIDDEN -> "No tienes permiso para acceder."
        CodigoError.RATE_LIMITED -> "Demasiados intentos. Espera un momento antes de intentar otra vez."
        CodigoError.SIN_CONEXION -> "No pudimos conectarnos. Revisa tu conexión."
        CodigoError.INTERNAL_ERROR -> "El servidor no pudo completar la solicitud. Inténtalo más tarde."
        CodigoError.RESPUESTA_INVALIDA -> "No pudimos interpretar la respuesta del servidor."
        CodigoError.GUARDADO_SESION -> "No pudimos guardar tu sesión. Inténtalo de nuevo."
    }
    else -> "No pudimos completar la solicitud. Inténtalo de nuevo."
}