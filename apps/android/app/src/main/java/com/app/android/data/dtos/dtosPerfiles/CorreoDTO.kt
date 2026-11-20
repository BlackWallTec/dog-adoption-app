package com.app.android.data.dtos.dtosPerfiles

import com.google.gson.annotations.SerializedName

data class CodigoCorreoRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("contrasena") val contrasena: String? = null,
    @SerializedName("confirmContrasena") val repetida: String? = null,
)
data class CodigoCorreoResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("message") val mensaje: String?,
)
