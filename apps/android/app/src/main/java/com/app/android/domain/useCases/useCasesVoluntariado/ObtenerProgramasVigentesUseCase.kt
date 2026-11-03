package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import javax.inject.Inject

class ObtenerProgramasVigentesUseCase @Inject constructor(
    private val repository: ProgramaRepository
) {
    suspend operator fun invoke(): List<Programa> {
        return repository.obtenerProgramasVigentes()
    }
}