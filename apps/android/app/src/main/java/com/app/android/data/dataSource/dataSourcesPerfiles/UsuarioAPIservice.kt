package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface UsuarioAPIservice {
    @POST("api/perfiles/login")
    suspend fun login(@Body body: LoginRequestDTO): LoginResponseDTO
}