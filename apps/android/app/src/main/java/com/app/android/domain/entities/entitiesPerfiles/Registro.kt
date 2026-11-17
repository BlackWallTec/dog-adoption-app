package com.app.android.domain.entities.entitiesPerfiles

/**
 * Datos de domain que la persona captura para crear una cuenta Cliente.
 * RegistroViewModel los actualiza y RegisterDTO los convierte al formato de POST /register.
 * Conserva el ID de la colonia seleccionada, los datos personales y la aceptación de privacidad.
 */
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
