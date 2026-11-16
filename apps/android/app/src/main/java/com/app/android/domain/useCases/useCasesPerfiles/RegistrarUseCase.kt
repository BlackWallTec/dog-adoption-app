package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.*
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class RegistrarUseCase @Inject constructor(private val repo: PerfilesRepositoryInt) {
    suspend operator fun invoke(registro: Registro): Result<Usuario> = repo.register(registro)
}
