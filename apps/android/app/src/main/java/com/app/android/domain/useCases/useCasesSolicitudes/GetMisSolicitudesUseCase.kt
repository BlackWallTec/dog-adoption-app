package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class GetMisSolicitudesUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        suspend operator fun invoke(): List<SolicitudCliente> = repository.getMisSolicitudes()
    }
