package com.app.android.data.dataSource.dataSourcesCatalogo

import retrofit2.http.GET

data class PerrosResponse(
    val success: Boolean,
    val data: List<PerroDto>
)

interface PerroApi {            ///
    @GET("api/catalogo/cliente/perros")
    suspend fun getPerros(): PerrosResponse           ///
}