package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.google.gson.annotations.SerializedName

// GET /profile responde { profile: {...} }
data class ProfileEnvelopeDTO(
    @SerializedName("profile") val profile: ProfileResponseDTO?,
)

data class ProfileResponseDTO(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellidos") val apellidos: String?,
    @SerializedName("fecha_nacimiento") val fecha_nacimiento: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("calle") val calle: String?,
    @SerializedName("numero_exterior") val numero_exterior: String?,
    @SerializedName("codigo_postal") val codigo_postal: String?,
)

fun ProfileResponseDTO.toDomain(): Perfil = Perfil(
    nombre = nombre.orEmpty(),
    apellidos = apellidos.orEmpty(),
    fechaNacimiento = fecha_nacimiento.orEmpty(),
    telefono = telefono.orEmpty(),
    calle = calle.orEmpty(),
    numeroExterior = numero_exterior.orEmpty(),
    codigoPostal = codigo_postal.orEmpty(),
)

fun ProfileEnvelopeDTO.toDomain(): Perfil =
    (profile ?: throw ErrorApi("No pudimos interpretar la respuesta del servidor.")).toDomain()
