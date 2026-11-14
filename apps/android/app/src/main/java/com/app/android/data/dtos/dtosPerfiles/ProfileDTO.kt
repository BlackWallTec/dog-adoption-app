package com.app.android.data.dtos.dtosPerfiles
import com.google.gson.annotations.SerializedName


data class ProfileResponseDTO(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellidos") val apellidos: String?,
    @SerializedName("fecha_nacimiento") val fecha_nacimiento: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("calle") val calle: String?,
    @SerializedName("numero_exterior") val numero_exterior: String?,
)