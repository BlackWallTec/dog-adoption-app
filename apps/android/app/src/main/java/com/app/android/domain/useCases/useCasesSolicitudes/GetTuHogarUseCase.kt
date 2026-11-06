package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

class GetTuHogarUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        operator fun invoke(): TuHogar {
            val actual =
                borrador.borrador.value
                    ?: throw IllegalStateException("No hay borrador iniciado")
            return actual.tuHogar ?: TuHogar()
        }
    }
