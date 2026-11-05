package com.app.android.domain.entities.entitiesPerfiles

fun Throwable.mensajePerfiles(): String = when (this) {
    is CredencialesInvalidasException -> "Correo o contraseña incorrectos."
    is RegistroInvalidoException -> "Revisa los campos indicados."
    is SesionVencidaException -> "Tu sesión venció. Inicia sesión de nuevo."
    is FalloAutenticacion -> when (motivo) {
        MotivoFalloAutenticacion.ACCESO_RESTRINGIDO -> "Tu cuenta no tiene acceso a esta función."
        MotivoFalloAutenticacion.DEMASIADOS_INTENTOS -> "Has realizado demasiados intentos. Espera 10 minutos y vuelve a intentarlo."
        MotivoFalloAutenticacion.SIN_CONEXION -> "No pudimos conectar con el refugio. Revisa tu conexión e inténtalo de nuevo."
        MotivoFalloAutenticacion.SERVIDOR -> "El servicio no está disponible en este momento. Inténtalo más tarde."
        MotivoFalloAutenticacion.RESPUESTA_INVALIDA -> "No pudimos comprobar la respuesta. Inténtalo de nuevo."
        MotivoFalloAutenticacion.GUARDADO_SESION -> "No pudimos guardar tu sesión. Intenta iniciar sesión de nuevo."
    }
    else -> "No pudimos completar la solicitud. Inténtalo de nuevo."
}
