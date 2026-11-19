package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class GetDatosGeneralesUseCase
@Inject
constructor(
    private val repository: SolicitudRepositoryInt,
    private val borrador: BorradorRepositoryInt,
) {
    suspend operator fun invoke(): DatosGenerales {
        val actual =
            borrador.borrador.value
                ?: throw IllegalStateException("No hay borrador iniciado")

        return actual.datosGenerales
            ?: repository.getPerfil()
    }
}
