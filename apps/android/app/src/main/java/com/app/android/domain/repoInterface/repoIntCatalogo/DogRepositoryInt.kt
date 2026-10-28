package com.app.android.domain.repoInterface.repoIntCatalogo

import com.app.android.domain.entities.entitiesCatalogo.Dog

interface DogRepositoryInt {
    // el "?" para permitir que devuelva null si el perro no existe
    suspend fun getDogById(id: String): Dog?

    suspend fun getDogs(): List<Dog>
}
