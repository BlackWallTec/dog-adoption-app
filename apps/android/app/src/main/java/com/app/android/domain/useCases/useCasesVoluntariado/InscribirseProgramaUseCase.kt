package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.validarInscripcion
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class InscribirseProgramaUseCase @Inject constructor(
    private val repository: InscripcionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(programa: Programa): Resultado<Inscripcion> {
        programa.validarInscripcion(Instant.now(clock))?.let { error ->
            return Resultado.Error(error)
        }
        return repository.inscribirse(programa.id)
    }
}