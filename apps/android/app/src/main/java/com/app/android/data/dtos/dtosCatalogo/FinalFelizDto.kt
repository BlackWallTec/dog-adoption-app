package com.app.android.data.dtos.dtosCatalogo

import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.google.gson.annotations.SerializedName

data class FinalesFelicesResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<FinalFelizDto>?,
)

data class FinalFelizDto(
    @SerializedName("id_perro") val idPerro: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("foto_hogar_url") val fotoHogarUrl: String?,
    @SerializedName("historia") val historia: String?,
    @SerializedName("fecha_adopcion") val fechaAdopcion: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("publicado") val publicado: Boolean?,
)

fun FinalFelizDto.toDomain(): FinalFeliz {
    val fecha = fechaAdopcion?.trim()?.ifBlank { null }
    val nombreLimpio = nombre?.trim().orEmpty()
    return FinalFeliz(
        idPerro = idPerro?.trim().orEmpty(),
        nombre = nombreLimpio.ifBlank { "Sin nombre" },
        fotoHogarUrl = fotoHogarUrl?.trim().orEmpty(),
        historia = historia?.trim().orEmpty(),
        fechaAdopcion = fecha,
        estado = estado?.trim().orEmpty(),
        publicado = publicado == true,
    )
}
