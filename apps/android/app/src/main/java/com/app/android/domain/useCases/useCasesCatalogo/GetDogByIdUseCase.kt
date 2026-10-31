package com.app.android.domain.useCases.useCasesCatalogo
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import javax.inject.Inject

class GetDogByIdUseCase
    @Inject
    constructor(
        private val dogRepositoryInt: DogRepositoryInt,
    ) {
        // Al usar "operator fun invoke", permites que se llame como getDogByIdUseCase(id)
        suspend operator fun invoke(id: String): Dog? {
            // Llama a la función getDogById del repositorio
            return dogRepositoryInt.getDogById(id)
        }
    }
