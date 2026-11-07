package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import javax.inject.Inject

class GetFilteredDogsUseCase
    @Inject
    constructor(
        private val repository: FilteredDogsRepository,
    ) {
        suspend operator fun invoke(filters: DogFilters): List<DogSummary> = repository.getDogs(filters)
    }