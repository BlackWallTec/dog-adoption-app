package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.ResultadoFiltrado
import com.app.android.domain.entities.entitiesVoluntariado.filtrarSegunReglas
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class FiltrarProgramasUseCase @Inject constructor(
    private val clock : Clock,
) {
    operator fun invoke(
        programas: List<Programa>,
        filtros: FiltrosProgramas,
        ): ResultadoFiltrado {
        return filtrarSegunReglas(
            programas,
            filtros,
            Instant.now(clock),
        )
    }
}