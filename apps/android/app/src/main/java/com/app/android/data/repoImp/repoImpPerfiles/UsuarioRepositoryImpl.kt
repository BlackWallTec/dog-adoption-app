package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.toDomain
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import retrofit2.HttpException
import javax.inject.Inject


// LOGIN - Respetando el contrato de DOMINIO de la interfaz (en este caso puede apreciarse en la firma), convierte los datos de entrada en un DTO (en el argumento), llama al servicio (login como método de la instancia de UsuarioAPIservice) y transforma esa respuesta (en una Sesion, lo que retorna el método toDomain).

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
) : PerfilesRepositoryInt {

    // Sobreescribe la función estipulada en la interfaz (Dominio)
    override suspend fun login(email: String, passwd: String): Sesion =
        try {
            // Este devuelve la sesión
            api.login(LoginRequestDTO(email, passwd)).toDomain()
        } catch (e: HttpException) {
            // Retrofit lanza HttpException en cualquier 4xx/5xx
            if (e.code() == 401) throw Exception("Credenciales inválidas (Unauthorized)")
        }
}