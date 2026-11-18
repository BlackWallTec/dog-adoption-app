package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.CodigosPostalesResponseDTO
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import com.app.android.data.dtos.dtosPerfiles.MeResponseDTO
import com.app.android.data.dtos.dtosPerfiles.ProfileEnvelopeDTO
import com.app.android.data.dtos.dtosPerfiles.RegisterRequestDTO
import com.app.android.data.dtos.dtosPerfiles.RegisterResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Gracias a AuthInterceptor, toda petición tiene el header de x-client
// Retrofit implementa esta interfaz: retrofit.create(UsuarioAPIservice::class.java).

interface UsuarioAPIservice {
    @POST("api/perfiles/login")
    suspend fun login(@Body body: LoginRequestDTO): LoginResponseDTO

    @POST("api/perfiles/register")
    suspend fun register(@Body body: RegisterRequestDTO): RegisterResponseDTO

    @GET("api/perfiles/profile")
    suspend fun profile(): ProfileEnvelopeDTO

    @GET("api/perfiles/me")
    suspend fun me(): MeResponseDTO

    @GET("api/perfiles/codigos-postales")
    suspend fun codigosPostales(@Query("cp") cp: String): CodigosPostalesResponseDTO
}