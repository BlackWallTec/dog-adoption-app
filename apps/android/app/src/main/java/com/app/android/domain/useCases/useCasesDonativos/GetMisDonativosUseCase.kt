package com.app.android.domain.useCases.useCasesDonativos

import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import javax.inject.Inject

class GetMisDonativosUseCase
    @Inject
    constructor(
        private val repository: DonativosRepositoryInt,
    ) {
        suspend operator fun invoke(): List<Donativo> = repository.getMisDonativos()
    }
