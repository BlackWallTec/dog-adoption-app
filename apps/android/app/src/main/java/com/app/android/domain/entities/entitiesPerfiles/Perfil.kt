package com.app.android.domain.entities.entitiesPerfiles

// GET /profile
data class Perfil(
    val nombre: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val telefono: String = "",
    val calle: String = "",
    val numeroExterior: String = "",
    val codigoPostal: String = "",
)