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

// filtros - repo
data class DogFilters(
    val sexo: String? = null,
    val tamanoId: String? = null,
    val razaId: String? = null,
    val edad: String? = null,
    val caracterIds: List<String> = emptyList(),
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