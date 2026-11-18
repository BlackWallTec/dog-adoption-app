package com.app.android.data.repoImp.repoImpCatalogo

import com.app.android.data.dataSource.dataSourcesCatalogo.CatalogFiltersApi
import com.app.android.data.dtos.dtosCatalogo.toDomain
import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoices
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import javax.inject.Inject

class FilteredDogsRepositoryImpl
    @Inject
    constructor(
        private val api: CatalogFiltersApi,
    ) : FilteredDogsRepository {
        override suspend fun getFilterChoices(): FilterChoices {
            val data = api.fetchFilterOptions().data
            return FilterChoices(
                tamanos = data?.tamanos.orEmpty().mapNotNull { it.toDomain() },
                razas = data?.razas.orEmpty().mapNotNull { it.toDomain() },
                caracteristicas = data?.caracteres.orEmpty().mapNotNull { it.toDomain() },
            )
        }

        override suspend fun getDogs(filters: DogFilters): List<DogSummary> {
            val response =
                api.fetchDogs(
                    sexo = filters.sexo,
                    tamano = filters.tamanoId,
                    raza = filters.razaId,
                    edad = filters.edad,
                    caracter = filters.caracterIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                )
            return response.data.orEmpty().map { it.toDomain() }
        }
    }