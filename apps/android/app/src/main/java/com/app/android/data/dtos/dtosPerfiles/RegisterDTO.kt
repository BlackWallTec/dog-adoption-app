package com.app.android.data.dtos.dtosPerfiles
import com.google.gson.annotations.SerializedName


data class RegisterRequestDTO(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("fecha_nacimiento") val fecha_nacimiento: String,
    @SerializedName("privacidad") val privacidad: Boolean,
    @SerializedName("id_codigo_postal") val id_codigo_postal: String,
    @SerializedName("calle") val calle: String,
    @SerializedName("numero_exterior") val numero_exterior: String,
    @SerializedName("numero_interior") val numero_interior: String?,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val correo: String,
)