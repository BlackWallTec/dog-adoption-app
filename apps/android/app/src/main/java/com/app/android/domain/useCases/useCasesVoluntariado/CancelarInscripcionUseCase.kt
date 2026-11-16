package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class CancelarInscripcionUseCase @Inject constructor(
    private val repository: InscripcionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(programa: Programa): Resultado<Inscripcion> {
        if (!programa.fechaInicio.isAfter(Instant.now(clock))) {
            return Resultado.Error(VoluntariadoError.ProgramaIniciado)
        }
        return repository.cancelarInscripcion(programa.id)
    }
}