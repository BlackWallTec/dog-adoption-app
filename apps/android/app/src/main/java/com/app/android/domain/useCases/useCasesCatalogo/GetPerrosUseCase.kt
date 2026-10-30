package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository
import jakarta.inject.Inject

class GetPerrosUseCase @Inject constructor(
    private val repo: PerroRepository
){
    suspend operator fun invoke(): List<Perro> = repo.getPerros()       //
}