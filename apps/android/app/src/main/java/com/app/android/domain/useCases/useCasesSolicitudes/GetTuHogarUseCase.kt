package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class GetTuHogarUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        suspend operator fun invoke(idSolicitud: String): TuHogar = repository.getTuHogar(idSolicitud) ?: TuHogar()
    }
