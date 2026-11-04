package com.app.android.data.repoImp.repoImpCatalogo
import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
<<<<<<< HEAD
import com.app.android.data.dtos.dtosCatalogo.DogDTO
=======
import com.app.android.data.dtos.dtosCatalogo.ApiResponseDetailDogDto
>>>>>>> develop
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
<<<<<<< HEAD
            val dogDTO: DogDTO = api.fetchDogByIdRemote(id)
=======
            val dogDTO: ApiResponseDetailDogDto = api.fetchDogByIdRemote(id)
>>>>>>> develop
            return dogDTO.toDomain()
        }

        override suspend fun getDogs(): List<Dog> {
<<<<<<< HEAD
            val dogDTOs: List<DogDTO> = api.fetchDogsRemote()
=======
            val dogDTOs: List<ApiResponseDetailDogDto> = api.fetchDogsRemote()
>>>>>>> develop
            return dogDTOs.map { it.toDomain() }
        }
    }
