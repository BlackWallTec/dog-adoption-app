package com.app.android.data.repoImp.repoImpPerfiles
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.dtos.dtosPerfiles.ApiResponseUsuarioDTO
import com.app.android.domain.entities.entitiesPerfil.Usuario
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class UsuarioRepositoryImpl
@Inject
constructor(
    private val api: UsuarioAPIservice,
) : PerfilesRepositoryInt {
    override suspend fun fetchByEmail(email: String): ApiResponseUsuarioDTO? {
        val usuarioDTO: ApiResponseUsuarioDTO = api.fetchByEmail(email)
        return usuarioDTO;
    }
}