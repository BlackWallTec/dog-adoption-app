package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.PaginatedDogs
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import javax.inject.Inject

class GetFilteredDogsUseCase
    @Inject
    constructor(
        private val repository: FilteredDogsRepository,
    ) {
        suspend operator fun invoke(filters: DogFilters): PaginatedDogs = repository.getDogs(filters)
    }
