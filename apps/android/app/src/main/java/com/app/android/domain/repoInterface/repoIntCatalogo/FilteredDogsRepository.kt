package com.app.android.domain.repoInterface.repoIntCatalogo

import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoices

interface FilteredDogsRepository {
    suspend fun getFilterChoices(): FilterChoices

    suspend fun getDogs(filters: DogFilters): List<DogSummary>
}