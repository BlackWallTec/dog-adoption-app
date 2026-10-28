package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository

class GetPerrosUseCase(private val repo: PerroRepository){
    suspend fun operator fun invoke(): List<Perro>=repo.getPerros()
}