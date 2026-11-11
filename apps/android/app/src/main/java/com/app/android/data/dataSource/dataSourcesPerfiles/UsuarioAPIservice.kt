package com.app.android.data.dataSource.dataSourcesPerfiles

import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.LoginResponseDTO
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


/**
La cabecera x-client: mobile permite que el backend identifique la petición como móvil y
devuelva el token en el JSON.
 **/
interface UsuarioAPIservice {
    @Headers("x-client: mobile")
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