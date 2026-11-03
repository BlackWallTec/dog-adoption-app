package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class GetCompromisoUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        // Lo que se guarda en la solicitud, vació si todavia no se han guardado datos
        suspend operator fun invoke(idSolicitud: String): Compromiso = repository.getCompromiso(idSolicitud) ?: Compromiso()
    }
