package com.app.android.data.dataSource.dataSourcesCatalogo

import retrofit2.http.GET
interface PerroApi {            ///
    @GET("api/catalogo/admin/perros")
    suspend fun getPerros(): List<PerroDto>             ///
}