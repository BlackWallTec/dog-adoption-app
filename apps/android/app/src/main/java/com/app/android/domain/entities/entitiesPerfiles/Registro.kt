package com.app.android.domain.entities.entitiesPerfiles

// Datos del formulario de registro (POST /register crea una cuenta Cliente).
data class Registro(
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val calle: String = "",
    val numeroExterior: String = "",
    // El backend lo valida como string: debe enviarse siempre (puede ser "").
    val numeroInterior: String = "",
    // id de la colonia elegida (id_codigo_postal), no el número postal.
    val idCodigoPostal: String = "",
    val contrasena: String = "",
    val confirmContrasena: String = "",
    val privacidad: Boolean = false,
)
