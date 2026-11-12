package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.domain.entities.entitiesCatalogo.paraCarrusel
import com.app.android.domain.repoInterface.repoIntCatalogo.FinalFelizRepository
import javax.inject.Inject

class GetFinalesFelicesUseCase
    @Inject
    constructor(
        private val repositorio: FinalFelizRepository,
    ) {
        suspend operator fun invoke(): List<FinalFeliz> = repositorio.getFinalesFelices().paraCarrusel()
    }