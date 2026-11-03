package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import retrofit2.http.Body
import retrofit2.http.POST

// LOGIN - Contrato, declaración de RETROFIT, en que se describe y configura la petición en método, ruta, cuerpo, respuesta, etc.
interface UsuarioAPIservice {
    @POST("api/perfiles/login")
    suspend fun login(@Body body: LoginRequestDTO): LoginResponseDTO
}

// Esta no debe implementarse, y con algo como 
// val api = retrofit.create(UsuarioAPIservice::class.java)
// le indica a Retrofit que implemente login.
// 
// Cuando la llamas, con:
// api.login(LoginRequestDTO(email, passwd))
// Retrofit se encargará de llevar a cabo la petición que fue configurada.