package com.app.android.data.dataSource.dataSourcesCatalogo

import com.app.android.data.dtos.dtosCatalogo.DogDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface DogApiService {
    @GET("v1/dogs/{id}")
    suspend fun fetchDogByIdRemote(
        @Path("id") id: String, // Reemplaza automáticamente "{id}" de la ruta con el String para el parámetro "id"
    ): DogDTO

    @GET("v1/dogs")
    suspend fun fetchDogsRemote(): List<DogDTO>
}
