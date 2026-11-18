package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.repoInterface.repoIntVoluntariado.DetalleProgramaRepository
import javax.inject.Inject

// HU36 consulta el detalle de un programa
class ObtenerDetalleProgramaUseCase
    @Inject
    constructor(
        private val repositorio: DetalleProgramaRepository,
    ) {
        suspend operator fun invoke(idPrograma: String): Resultado<DetallePrograma> = repositorio.obtenerDetalle(idPrograma)
    }
