package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class CerrarSesionUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke() = repoInt.logout()
}