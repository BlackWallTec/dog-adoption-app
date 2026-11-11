package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.google.gson.annotations.SerializedName

/**
 corrigo correo, contraseña y el uuid
 **/
data class LoginRequestDTO(
    @SerializedName("correo") val email: String,
    @SerializedName("contrasena") val passwd: String,
)

data class LoginResponseDTO(
    @SerializedName("user") val user: UserDTO?,
    @SerializedName("token") val token: String?,
)

data class UserDTO(
    @SerializedName("uuid") val id: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("rol") val rol: String?,
)

fun LoginResponseDTO.toDomain(): Sesion {
    val u = user ?: error("Respuesta sin usuario")
    return Sesion(
        usuario = Usuario(
            id = u.id ?: error("Respuesta sin id"),
            email = u.email.orEmpty(),
            rol = u.rol.orEmpty(),
        ),
        token = token ?: error("Respuesta sin token"),
    )
}