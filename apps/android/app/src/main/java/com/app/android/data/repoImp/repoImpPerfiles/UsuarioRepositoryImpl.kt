package com.app.android.data.repoImp.repoImpPerfiles
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.authDTO
import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import retrofit2.HttpException
import javax.inject.Inject


class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice, ) : PerfilesRepositoryInt {

    override suspend fun login(
        correo: String, contrasena: String): Sesion {
        val correo = correo.trim()

        return try {
            val sesion = api.login(
                LoginRequestDTO(correo, contrasena)).toDomain()

        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw CredencialesInvalidasException()
            }
            throw e
        }
    }
    
    override suspend fun register() {
    }
    
    override suspend fun logout() {
    }
    
    override suspend fun profile() {
    }
    
    override suspend fun me() {
    }
    
    override suspend fun codigosPostales() {
    }
}