package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import com.app.android.data.dtos.dtosPerfiles.RegistroRequestDTO
import com.app.android.data.dtos.dtosPerfiles.SesionResponseDTO
import com.app.android.data.dtos.dtosPerfiles.PerfilResponseDTO
import com.app.android.data.dtos.dtosPerfiles.LogoutResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// LOGIN - Contrato, declaración de RETROFIT, en que se describe y configura la petición en método, ruta, cuerpo, respuesta, etc.
interface UsuarioAPIservice {
    @POST("api/perfiles/login")
    suspend fun login(@Body body: LoginRequestDTO): LoginResponseDTO
    @POST("api/perfiles/register")
    suspend fun registrar(@Body body: RegistroRequestDTO): LoginResponseDTO
    @POST("api/perfiles/me")
    suspend fun sesion(): SesionResponseDTO
    @GET("api/perfiles/profile")
    suspend fun perfil(): PerfilResponseDTO
    @POST("api/perfiles/logout")
    suspend fun logout(): LogoutResponseDTO
}

// Esta no debe implementarse, y con algo como
// val api = retrofit.create(UsuarioAPIservice::class.java)
// le indica a Retrofit que implemente login.
//
// Cuando la llamas, con:
// api.login(LoginRequestDTO(email, passwd))
// Retrofit se encargará de llevar a cabo la petición que fue configurada.
