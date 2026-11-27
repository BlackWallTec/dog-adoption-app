package com.app.android.data.dtos.dtosCatalogo

import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoice
import com.app.android.domain.entities.entitiesCatalogo.PaginatedDogs
import com.app.android.domain.entities.entitiesCatalogo.PaginationMeta
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

// get perros con metadatos de paginación
data class FilteredDogsResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<FilteredDogDto>?,
    @SerializedName("meta") val meta: PaginationMetaDto?, // Metadatos del backend
)

data class PaginationMetaDto(
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("last_page") val lastPage: Int?,
    @SerializedName("has_more") val hasMore: Boolean?,
    @SerializedName("next_cursor") val nextCursor: String?,
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
    val primeraImagen = fotoUrl ?: multimedia?.firstOrNull()?.url

    return DogSummary(
        id = idPerro ?: "",
        nombre = nombre?.trim() ?: "Sin nombre",
        edadAnios = edadAprox ?: 0,
        sexo = sexo?.trim() ?: "",
        tamano = tamano?.nombre ?: "",
        raza = raza?.nombre ?: "Sin raza",
        fotoUrl = primeraImagen?.takeIf { it.isNotBlank() },
    )
}

// == Mapeo de la metainformación de paginación a Dominio ==
fun PaginationMetaDto?.toDomain(): PaginationMeta =
    PaginationMeta(
        currentPage = this?.currentPage ?: 1,
        perPage = this?.perPage ?: 10,
        total = this?.total ?: 0,
        lastPage = this?.lastPage ?: 1,
        hasMore = this?.hasMore ?: false,
        nextCursor = this?.nextCursor,
    )

// 👈 Mapeo completo de la respuesta de perros paginada a Dominio
fun FilteredDogsResponseDto.toDomain(): PaginatedDogs =
    PaginatedDogs(
        dogs = data.orEmpty().map { it.toDomain() },
        meta = meta.toDomain(),
    )
