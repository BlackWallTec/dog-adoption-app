package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.google.gson.annotations.SerializedName

data class RegisterRequestDTO(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String,
    @SerializedName("privacidad") val privacidad: Boolean,
    @SerializedName("id_codigo_postal") val idCodigoPostal: String,
    @SerializedName("calle") val calle: String,
    @SerializedName("numero_exterior") val numeroExterior: String,
    @SerializedName("numero_interior") val numeroInterior: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("confirmContrasena") val confirmContrasena: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String,
)

data class RegisterResponseDTO(
    @SerializedName("user") val user: UserDTO?,
)

fun Registro.toRequest() = RegisterRequestDTO(
    nombre = nombre.trim(),
    apellidos = apellidos.trim(),
    fechaNacimiento = fechaNacimiento,
    privacidad = privacidad,
    idCodigoPostal = idCodigoPostal,
    calle = calle.trim(),
    numeroExterior = numeroExterior.trim(),
    numeroInterior = numeroInterior.trim(),
    contrasena = contrasena,
    confirmContrasena = confirmContrasena,
    correo = correo.trim().lowercase(),
    telefono = telefono.trim().removePrefix("+"),
)
