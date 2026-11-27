package com.app.android.data.dataSource.dataSourcesCatalogo

import com.app.android.data.dtos.dtosCatalogo.FilterOptionsResponseDto
import com.app.android.data.dtos.dtosCatalogo.FilteredDogsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// ESto aun no esta
interface CatalogFiltersApi {
    @GET("api/catalogo/cliente/filtros")
    suspend fun fetchFilterOptions(): FilterOptionsResponseDto

    @GET("api/catalogo/cliente/perros")
    suspend fun fetchDogs(
        @Query("sexo") sexo: String? = null,
        @Query("tamano") tamano: String? = null,
        @Query("raza") raza: String? = null,
        @Query("edad") edad: String? = null,
        // ids separados por coma
        @Query("caracter") caracter: String? = null,
        // Limite de paginacion
        @Query("limit") limit: Int = 10,
        // Offset de paginacion
        @Query("offset") offset: Int = 0,
    ): FilteredDogsResponseDto
}
