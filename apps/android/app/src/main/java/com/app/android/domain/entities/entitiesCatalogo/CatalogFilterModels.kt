package com.app.android.domain.entities.entitiesCatalogo

// filtro
data class FilterChoice(
    val id: String,
    val nombre: String,
)

data class FilterChoices(
    val tamanos: List<FilterChoice> = emptyList(),
    val razas: List<FilterChoice> = emptyList(),
    val caracteristicas: List<FilterChoice> = emptyList(),
)

// filtros - repo (con soporte de paginación)
data class DogFilters(
    val sexo: String? = null,
    val tamanoId: String? = null,
    val razaId: String? = null,
    val edad: String? = null,
    val caracterIds: List<String> = emptyList(),
    val limit: Int = 10,
    val offset: Int = 0,
)

// resumen para la lista
data class DogSummary(
    val id: String,
    val nombre: String,
    val edadAnios: Int,
    val sexo: String,
    val tamano: String,
    val raza: String,
    val fotoUrl: String? = null,
)

// ==  Metadata de paginación para el Dominio ==
data class PaginationMeta(
    val currentPage: Int = 1,
    val perPage: Int = 10,
    val total: Int = 0,
    val lastPage: Int = 1,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
)

// == Contenedor con la lista de perros + la información de paginación ==
data class PaginatedDogs(
    val dogs: List<DogSummary> = emptyList(),
    val meta: PaginationMeta = PaginationMeta(),
)
