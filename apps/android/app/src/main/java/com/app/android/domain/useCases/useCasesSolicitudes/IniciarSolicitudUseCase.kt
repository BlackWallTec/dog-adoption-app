package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class IniciarSolicitudUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
    ) {
        // Se llama como inicarSolicitudUseCase (idUsuario, idPerro) noteee
        suspend operator fun invoke(
            idUsuario: String,
            idPerro: String,
        ): Solicitud {
            // SI hay existe un borrador ps lo regresa (para continuar), si es null (?:) epieza de 0
            return repository.buscarBorrador(idUsuario,idPerro)
                ?: repository.crearSolicitud(idUsuario, idPerro)
        }
    }