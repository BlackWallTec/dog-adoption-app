package com.app.android.data.dtos.dtosCatalogo

import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoice
import com.google.gson.annotations.SerializedName

// get
data class FilterOptionsResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: FilterOptionsDataDto?,
)

data class FilterOptionsDataDto(
    @SerializedName("razas") val razas: List<FilterChoiceDto>?,
    @SerializedName("tamanos") val tamanos: List<FilterChoiceDto>?,
    @SerializedName("caracteres") val caracteres: List<FilterChoiceDto>?,
)

data class FilterChoiceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
)

// get perros
data class FilteredDogsResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<FilteredDogDto>?,
)

data class FilteredDogDto(
    @SerializedName("id_perro") val idPerro: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("sexo") val sexo: String?,
    @SerializedName("edad_aprox") val edadAprox: Int?,
    @SerializedName("raza") val raza: CatalogNameDto?,
    @SerializedName("tamano") val tamano: CatalogNameDto?,
    @SerializedName("foto_url") val fotoUrl: String?,
    @SerializedName("multimedia") val multimedia: List<MultimediaFilterDto>?,
)

data class MultimediaFilterDto(
    @SerializedName("url") val url: String?,
    @SerializedName("tipo") val tipo: String?,
)

data class CatalogNameDto(
    @SerializedName("nombre") val nombre: String?,
)

fun FilterChoiceDto.toDomain(): FilterChoice? {
    val idSeguro = id ?: return null
    val nombreSeguro = nombre?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return FilterChoice(id = idSeguro, nombre = nombreSeguro)
}

fun FilteredDogDto.toDomain(): DogSummary {
    // Busca primero en "foto_url" (si el backend lo envía)
    // o toma la URL del primer objeto dentro del array "multimedia"
    val primeraImagen = fotoUrl ?: multimedia?.firstOrNull()?.url

    return DogSummary(
        id = idPerro ?: "",
        nombre = nombre?.trim() ?: "Sin nombre",
        edadAnios = edadAprox ?: 0,
        sexo = sexo?.trim() ?: "",
        tamano = tamano?.nombre ?: "",
        raza = raza?.nombre ?: "Sin raza",
        fotoUrl = primeraImagen?.takeIf { it.isNotBlank() }, // 👈 Asigna la imagen obtenida
    )
}
