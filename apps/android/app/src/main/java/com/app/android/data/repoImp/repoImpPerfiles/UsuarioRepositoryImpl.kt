package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.toDomain
import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Convierte el error 401 en la excepción que reconoce LoginViewModel.
 * Relanza los demás errores HTTP.
 * Conserva el correo utilizado para iniciar sesión, porque el backend no lo devuelve
 * **/

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
) : PerfilesRepositoryInt {

    override suspend fun login(email: String, passwd: String): Sesion {
        val correo = email.trim()

        return try {
            val sesion = api.login(LoginRequestDTO(correo, passwd)).toDomain()

            sesion.copy(
                usuario = sesion.usuario.copy(email = correo)
            )
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw CredencialesInvalidasException()
            }
            throw e
        }
    }
}