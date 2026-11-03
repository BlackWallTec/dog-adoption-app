package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class GetDatosGeneralesUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        suspend operator fun invoke (
            idSolicitud: String,
            idUsuario: String,
        ): DatosGenerales {
            // 1. Lo que ya guardo en esta solicitud tiene prioridad
            // 2. Si no hay nada, se precarga lo que se puede desde el perfil
            return repository.getDatosGenerales(idSolicitud)
                ?: repository.getPerfil(idUsuario)
        }
    }