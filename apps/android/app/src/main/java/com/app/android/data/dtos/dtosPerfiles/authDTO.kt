package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.google.gson.annotations.SerializedName

// Para endpoint de postLogin, inicio de sesión
data class LoginRequestDTO(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
)

data class LoginResponseDTO(
    @SerializedName("user") val user: UserDTO?,
    @SerializedName("token") val token:  String?,
)

// Para endpoint postLogout, cierra sesión
data class LogoutResponseDTO(
    @SerializedName("ok") val ok: Boolean?,
)

// Para endpoint getMe, corrobora info de sesión
data class MeResponseDTO(
    @SerializedName("user") val user: UserDTO?,
)

// Clase auxiliar
data class UserDTO(
    // URGE: Revisar el Log respecto a este id.
    @SerializedName("id") val id: String?,
    @SerializedName("rol") val rol: String?,
)


// Asegurar viabilidad de login con la respuesta 

// De forma implícita, esta función se refiere a
// una instancia de LoginResponseDTO, que devuelve
// una sesión (entidad importada desde dominio)

fun LoginResponseDTO.toDomain(): Sesion {
    val usuarioDto = user ?: error("Respuesta sin usuario")
    val id = usuarioDto.id ?: error("Respuesta sin id")
    val rol = usuarioDto.rol ?: error("Usuario sin rol")

    require(rol == "Cliente") {
        "Rol distinto de cliente: $rol"
    }

    return Sesion(
        usuario = Usuario(
            id = id,
            rol = rol,
        ),
        token = token ?: error("Respuesta sin token"),
    )
}