package com.app.android.domain.entities.entitiesPerfiles

data class Perfil(
    val nombre: String = "", val apellidos: String = "", val telefono: String = "",
    val fechaNacimiento: String = "", val calle: String = "", val numeroExterior: String = "",
    val codigoPostal: String = "",
)

class SesionVencidaException : Exception("Tu sesión venció. Inicia sesión de nuevo.")
