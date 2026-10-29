package com.app.android.data.repoImp.repoImpCatalogo
import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.data.dtos.dtosCatalogo.ApiResponseDetailDogDto
import com.app.android.data.dtos.dtosCatalogo.toDomain
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import javax.inject.Inject

class DogRepositoryImpl
    @Inject
    constructor(
        private val api: DogApiService,
    ) : DogRepositoryInt {
        override suspend fun getDogById(id: String): Dog? {
            val dogDTO: ApiResponseDetailDogDto = api.fetchDogByIdRemote(id)
            return dogDTO.toDomain()
        }

        override suspend fun getDogs(): List<Dog> {
            val dogDTOs: List<ApiResponseDetailDogDto> = api.fetchDogsRemote()
            return dogDTOs.map { it.toDomain() }
        }
    }
