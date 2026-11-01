package com.app.android.data.dataSource.dataSourcesPerfiles

import retrofit2.http.GET
import retrofit2.http.Path

// 2. Interface de Servicio (API / Supabase)
interface UsuarioAPIservice {
    @GET("endpoint/{id}")
    suspend fun fetchDatos(@Path("id") id: String): NombreDto
}