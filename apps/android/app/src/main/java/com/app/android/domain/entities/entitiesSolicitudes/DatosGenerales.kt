package com.app.android.domain.entities.entitiesSolicitudes

// ENTITY: los datos generales (segundo paso de la solicitud)  del adoptante para una solicitud.
// Es lo que la app entiende, no como esta guardado en la BD.
data class DatosGenerales(
    val nombreCompleto: String,
    val fechaNacimiento: String?, // formato "1995-03-15"; puede no venir
    val ocupacion: String,
    val domicilio: String,
    val municipio: String,
    val telefono: String,
    val correo: String,
)