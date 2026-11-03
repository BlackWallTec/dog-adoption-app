package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.FilterChoices
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import javax.inject.Inject

class GetFilterChoicesUseCase
    @Inject
    constructor(
        private val repository: FilteredDogsRepository,
    ) {
        suspend operator fun invoke(): FilterChoices = repository.getFilterChoices()
    }