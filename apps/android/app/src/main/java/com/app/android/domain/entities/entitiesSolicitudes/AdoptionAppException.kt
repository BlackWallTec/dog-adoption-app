package com.app.android.domain.entities.entitiesSolicitudes

// Generado por IA, el AdoptionAppException nos sirve para saber que tipo de error nuestra app (no la BD) entiende
// Seria como definir la "causa"  de un fallo
// Se utiliza tanto en el Repository como en el ViewModel
// Este creo que servira mejor hacerlo compartido para que todos pongan sus tipos de errores en el mismo lugar
sealed class AdoptionAppException(
    message: String? = null,
) : Exception(message) {
    object NoConnection : AdoptionAppException() // Error de conexión

    object UnAuthorized : AdoptionAppException() // Error de autenticación

    class Rejected(
        message: String?,
    ) : AdoptionAppException(message) // Error tipo 400/402
}
