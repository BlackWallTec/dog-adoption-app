package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Registro
import com.google.gson.annotations.SerializedName

data class RegistroRequestDTO(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String,
    @SerializedName("calle") val calle: String,
    @SerializedName("numeroExterior") val numeroExterior: String,
    @SerializedName("codigoPostal") val codigoPostal: String,
    @SerializedName("passwd") val passwd: String,
    @SerializedName("confirmPasswd") val confirmPasswd: String,
    @SerializedName("privacidad") val privacidad: Boolean,
)

fun Registro.toDTO() = RegistroRequestDTO(nombre, apellidos, email, telefono, fechaNacimiento, calle, numeroExterior, codigoPostal, passwd, confirmPasswd, privacidad)

data class SesionResponseDTO(@SerializedName("user") val user: UserDTO?)
data class PerfilResponseDTO(@SerializedName("profile") val profile: PerfilDTO?)
data class LogoutResponseDTO(@SerializedName("ok") val ok: Boolean)
data class ErrorRegistroDTO(@SerializedName("fields") val fields: Map<String, String>?)

data class PerfilDTO(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellidos") val apellidos: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("calle") val calle: String?,
    @SerializedName("numeroExterior") val numeroExterior: String?,
    @SerializedName("codigoPostal") val codigoPostal: String?,
) {
    fun toDomain() = Perfil(nombre.orEmpty(), apellidos.orEmpty(), telefono.orEmpty(), fechaNacimiento.orEmpty(), calle.orEmpty(), numeroExterior.orEmpty(), codigoPostal.orEmpty())
}
