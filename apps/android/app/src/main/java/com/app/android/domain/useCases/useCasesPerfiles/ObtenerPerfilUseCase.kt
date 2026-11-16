package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class ObtenerPerfilUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    suspend operator fun invoke(): Result<Perfil> = repo.profile()
}
