package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.authDTO
import retrofit2.http.Body
import retrofit2.http.POST


/**
 La cabecera x-client: mobile permite que el backend identifique la petición como móvil y devuelva el token en el JSON.
 
 Antes la ponía aquí, pero ahora va en el AuthInterceptor 
 **/

interface UsuarioAPIservice {
    @POST("api/perfiles/login") suspend fun login(@Body body: LoginRequestDTO): LoginResponseDTO
    @POST("api/perfiles/register") suspend fun register(@Body body: RegisterRequestDTO)
    @POST("api/perfiles/logout") suspend fun logout()
    @GET("api/perfiles/profile") suspend fun profile(): ProfileResponseDTO
    @GET("api/perfiles/me") suspend fun me(): MeResponseDTO
    @GET("api/perfiles/codigos-postales") suspend fun codigosPostales(@Query("cp") cp: String): CodigosPostalesResponseDTO
}

// Esta no debe implementarse, y con algo como 
// val api = retrofit.create(UsuarioAPIservice::class.java)
// le indica a Retrofit que implemente login.
// 
// Cuando la llamas, con:
// api.login(LoginRequestDTO(email, passwd))
// Retrofit se encargará de llevar a cabo la petición que fue configurada.