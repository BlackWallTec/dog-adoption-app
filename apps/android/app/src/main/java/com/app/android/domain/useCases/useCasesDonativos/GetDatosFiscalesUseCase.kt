package com.app.android.domain.useCases.useCasesDonativos

import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.repoInterface.repoIntDonativos.DatosFiscalesRepositoryInt
import javax.inject.Inject

class GetDatosFiscalesUseCase
    @Inject
    constructor(
        private val repository: DatosFiscalesRepositoryInt,
    ) {
        suspend operator fun invoke(idUsuario: String): DatosFiscales = repository.getDatosFiscales(idUsuario)
    }
