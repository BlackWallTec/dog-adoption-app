package com.app.android.data.repoImp.repoImpCatalogo

import com.app.android.data.dataSource.dataSourcesCatalogo.CatalogFiltersApi
import com.app.android.data.dtos.dtosCatalogo.toDomain
import com.app.android.data.local.preferences.DogPreferences
import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoices
import com.app.android.domain.entities.entitiesCatalogo.PaginatedDogs
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import javax.inject.Inject

class FilteredDogsRepositoryImpl
    @Inject
    constructor(
        private val api: CatalogFiltersApi,
        private val preferences: DogPreferences,
    ) : FilteredDogsRepository {
        override suspend fun getFilterChoices(): FilterChoices {
            val data = api.fetchFilterOptions().data
            return FilterChoices(
                tamanos = data?.tamanos.orEmpty().mapNotNull { it.toDomain() },
                razas = data?.razas.orEmpty().mapNotNull { it.toDomain() },
                caracteristicas = data?.caracteres.orEmpty().mapNotNull { it.toDomain() },
            )
        }

        override suspend fun getDogs(filters: DogFilters): PaginatedDogs {
            val isFirstPage = (filters.offset == 0)
            val sinFiltros =
                filters.sexo == null &&
                    filters.tamanoId == null &&
                    filters.razaId == null &&
                    filters.edad == null &&
                    filters.caracterIds.isEmpty()

            // 1. Cache First (si es válida, ahorramos la petición a red)
            if (isFirstPage && sinFiltros && preferences.isCacheValid()) {
                preferences.getCachedPaginatedDogs()?.let { cachedData ->
                    return cachedData
                }
            }

            // 2. Fetch Network con respaldo de caché en caso de error
            return try {
                val response =
                    api.fetchDogs(
                        sexo = filters.sexo,
                        tamano = filters.tamanoId,
                        raza = filters.razaId,
                        edad = filters.edad,
                        caracter = filters.caracterIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                        limit = filters.limit,
                        offset = filters.offset,
                    )
                val paginated = response.toDomain()

                if (sinFiltros) {
                    preferences.savePaginatedDogs(
                        newDogs = paginated.dogs,
                        newOffset = filters.offset + paginated.dogs.size,
                        totalCount = response.meta?.total ?: paginated.dogs.size,
                        isFirstPage = isFirstPage,
                    )
                }
                paginated
            } catch (e: Exception) {
                // 3. FALLBACK OFFLINE: Si no hay red, entregar el caché deshabilitando la paginación (hasMore = false)
                if (isFirstPage && sinFiltros) {
                    preferences.getCachedPaginatedDogs()?.let { staleCache ->
                        return staleCache.copy(
                            meta = staleCache.meta.copy(hasMore = false), // 👈 DESACTIVA EL SCROLL INFINITO SIN RED
                        )
                    }
                }
                throw e
            }
        }
    }
