package com.app.android.domain.useCases.useCasesPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import javax.inject.Inject

/** null si no hay token guardado o ya venció; un fallo de red se propaga. */
class RestaurarSesionUseCase @Inject constructor(
    private val repoInt: PerfilesRepositoryInt,
) {
    suspend operator fun invoke(): Sesion? = repoInt.sesionActual()
}