package com.app.android.data.dataSource.dataSourcesCatalogo

import com.app.android.data.dtos.dtosCatalogo.ApiResponseCatalogDogsDto
import com.app.android.data.dtos.dtosCatalogo.ApiResponseDetailDogDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DogApiService {
    @GET("api/catalogo/cliente/perros/{id}")
    suspend fun fetchDogByIdRemote(
        @Path("id") id: String, // Reemplaza automáticamente "{id}" de la ruta con el String para el parámetro "id"
    ): ApiResponseDetailDogDto

    @GET("api/catalogo/admin/perros")
    suspend fun fetchDogsRemote(): List<ApiResponseDetailDogDto>
}
