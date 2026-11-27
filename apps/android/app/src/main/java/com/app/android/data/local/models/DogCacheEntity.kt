package com.app.android.data.local.models

import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.google.gson.annotations.SerializedName

/**
 * Representa la estructura del perro que se guarda localmente como JSON
 * en SharedPreferences cuando no hay internet o para acelerar la carga.
 */
data class DogCacheEntity(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("edad_anios") val edadAnios: Int,
    @SerializedName("sexo") val sexo: String,
    @SerializedName("tamano") val tamano: String,
    @SerializedName("raza") val raza: String,
    @SerializedName("foto_url") val fotoUrl: String? = null,
) {
    /** Convierte el objeto guardado en Caché a la entidad limpia de Dominio */
    fun toDomain(): DogSummary =
        DogSummary(
            id = id,
            nombre = nombre,
            edadAnios = edadAnios,
            sexo = sexo,
            tamano = tamano,
            raza = raza,
            fotoUrl = fotoUrl,
        )
}

/** Convierte una entidad de Dominio a objeto de Caché para guardarlo */
fun DogSummary.toCacheEntity(): DogCacheEntity =
    DogCacheEntity(
        id = id,
        nombre = nombre,
        edadAnios = edadAnios,
        sexo = sexo,
        tamano = tamano,
        raza = raza,
        fotoUrl = fotoUrl,
    )
