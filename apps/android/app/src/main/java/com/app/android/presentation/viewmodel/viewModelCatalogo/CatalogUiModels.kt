package com.app.android.presentation.viewmodel.viewModelCatalogo

data class FilterSelection(
    val tamanoIndex: Int = -1,
    val edadIndex: Int = -1,
    val sexoIndex: Int = -1,
    val razaIndex: Int = -1,
    val caracteristicas: Set<Int> = emptySet()
) {
    val activeCount: Int
        get() = (if (tamanoIndex >= 0) 1 else 0) +
                (if (edadIndex >= 0) 1 else 0) +
                (if (sexoIndex >= 0) 1 else 0) +
                (if (razaIndex >= 0) 1 else 0) +
                caracteristicas.size

    val isEmpty: Boolean
        get() = activeCount == 0
}

data class FilterOptions(
    val tamanos: List<String> = listOf("Chico", "Mediano", "Grande"),
    val edades: List<String> = listOf("Cachorro", "Adulto", "Senior"),
    val sexos: List<String> = listOf("Macho", "Hembra"),
    val razas: List<String> = listOf("Mestizo", "Labrador", "Pastor alemán"),
    val caracteristicas: List<String> = listOf("Convive con gatos", "Cuidados especiales")
)

data class DogCardUi(
    val id: String,
    val nombre: String,
    val edad: String,
    val sexo: String,
    val tamano: String,
    val raza: String,
    val fotoUrl: String? = null
)

sealed interface CatalogUiState {
    object Loading : CatalogUiState
    object Empty : CatalogUiState
    data class Success(val perros: List<DogCardUi>) : CatalogUiState
    data class Error(val mensaje: String) : CatalogUiState
}