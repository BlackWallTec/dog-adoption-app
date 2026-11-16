package com.app.android.data.dtos.dtosPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.google.gson.annotations.SerializedName

// GET /codigos-postales?cp= responde { data: [...] } (data: [] si el CP no existe)
data class CodigosPostalesResponseDTO(
    @SerializedName("data") val data: List<cpsResponseDTO>?,
)

data class cpsResponseDTO(
    @SerializedName("id_codigo_postal") val id_codigo_postal: String?,
    @SerializedName("codigo_postal") val codigo_postal: String?,
    @SerializedName("colonia") val colonia: String?,
    @SerializedName("municipio") val municipio: String?,
    @SerializedName("estado") val estado: String?,
)

// Sin id o sin código no se puede elegir la colonia: se descarta. colonia nula pasa a "".
fun cpsResponseDTO.toDomain(): CodigoPostal? {
    val id = id_codigo_postal?.takeIf { it.isNotBlank() } ?: return null
    val cp = codigo_postal?.takeIf { it.isNotBlank() } ?: return null
    return CodigoPostal(
        idCodigoPostal = id,
        codigoPostal = cp,
        colonia = colonia.orEmpty(),
        municipio = municipio.orEmpty(),
        estado = estado.orEmpty(),
    )
}

fun CodigosPostalesResponseDTO.toDomain(): List<CodigoPostal> =
    (data ?: throw ErrorApi("No pudimos interpretar la respuesta del servidor."))
        .mapNotNull { it.toDomain() }
