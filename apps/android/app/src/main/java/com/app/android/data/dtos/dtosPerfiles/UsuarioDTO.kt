package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfil.Usuario
import com.google.gson.annotations.SerializedName

// 1. Envoltorio Global de la API
data class ApiResponseUsuarioDTO(
    @SerializedName("user") val success: List<UserDTO>?,
    @SerializedName("token") val data: String?,
)

// 2. Info del usuario
data class UserDTO(
    @SerializedName("id_usuario") val idUsuario: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("rol") val rol: String?,
)