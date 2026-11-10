package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

class GetCompromisoUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        // Lo que se guarda en la solicitud, vació si todavia no se han guardado datos
        operator fun invoke(): Compromiso {
            val actual =
                borrador.borrador.value
                    ?: throw IllegalStateException("No hay borrador iniciado")
            return actual.compromiso ?: Compromiso()
        }
    }
