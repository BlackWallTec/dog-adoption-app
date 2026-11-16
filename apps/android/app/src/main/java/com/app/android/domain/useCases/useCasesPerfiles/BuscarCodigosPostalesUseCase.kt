package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

class BuscarCodigosPostalesUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke(cp: String): List<CodigoPostal> = repoInt.codigosPostales(cp)
}