package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.google.gson.annotations.SerializedName

// POST /login
data class LoginRequestDTO(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
)

data class LoginResponseDTO(
    @SerializedName("user") val user: UserDTO?,
    @SerializedName("token") val token: String?,
)

// GET /me
data class MeResponseDTO(
    @SerializedName("user") val user: UserDTO?,
)

// Clase auxiliar: la API responde { uuid, rol }
data class UserDTO(
    @SerializedName("uuid") val id: String?,
    @SerializedName("rol") val rol: String?,
)

private fun respuestaInvalida(): Nothing =
    throw ErrorApi(CodigoError.RESPUESTA_INVALIDA)

// Receptor nullable: sirve también para RegisterResponseDTO.user y MeResponseDTO.user.
// Solo valida la forma de la respuesta; la regla "solo Cliente" vive en LoginUseCase.
fun UserDTO?.toDomain(): Usuario {
    val dto = this ?: respuestaInvalida()
    return Usuario(
        id = dto.id?.takeIf { it.isNotBlank() } ?: respuestaInvalida(),
        rol = dto.rol?.takeIf { it.isNotBlank() } ?: respuestaInvalida(),
    )
}

fun LoginResponseDTO.toDomain(): Sesion = Sesion(
    usuario = user.toDomain(),
    token = token?.takeIf { it.isNotBlank() } ?: respuestaInvalida(),
)