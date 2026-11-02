package com.app.android.data.repoImp.repoImpPerfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.LoginRequestDTO
import com.app.android.data.dtos.dtosPerfiles.toDomain
import com.app.android.domain.entities.entitiesPerfiles.CredencialesInvalidasException
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import retrofit2.HttpException
import javax.inject.Inject

class UsuarioRepositoryImpl @Inject constructor(
    private val api: UsuarioAPIservice,
) : PerfilesRepositoryInt {
    override suspend fun login(email: String, passwd: String): Sesion =
        try {
            api.login(LoginRequestDTO(email, passwd)).toDomain()
        } catch (e: HttpException) {
            // Retrofit lanza HttpException en cualquier 4xx/5xx
            if (e.code() == 401) throw CredencialesInvalidasException()
            throw e
        }
}