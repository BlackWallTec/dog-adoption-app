package com.app.android.data.dataSource.dataSourcesPerfiles

import retrofit2.http.GET
import retrofit2.http.Path

// 2. Interface de Servicio (API / Supabase)
interface UsuarioAPIservice {
    @POST("api/perfiles/login/")
    suspend fun fetchByEmail(@Body email: String): ApiResponseUsuarioDTO
}