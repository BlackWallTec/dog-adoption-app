package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.google.gson.annotations.SerializedName

data class LoginRequestDTO(
    @SerializedName("email") val email: String,
    @SerializedName("passwd") val passwd: String,
)

data class LoginResponseDTO(
    @SerializedName("user") val user: UserDTO?,
    @SerializedName("token") val token: String?,
)

data class UserDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("rol") val rol: String?,
)

fun LoginResponseDTO.toDomain(): Sesion {
    val u = user ?: error("Respuesta sin usuario")
    check(!token.isNullOrBlank()) { "Respuesta sin token" }
    return Sesion(usuario = u.toDomain(), token = token)
}

fun UserDTO.toDomain(): Usuario {
    check(!id.isNullOrBlank() && !email.isNullOrBlank() && !rol.isNullOrBlank()) { "Respuesta de usuario incompleta" }
    return Usuario(id, email, rol)
}
