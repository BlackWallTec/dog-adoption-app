package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// Resultado de intentar empezar una solicitud
sealed interface ResultadoInicio {
    // No tiene solicitud activa: el borrador ya se inició, puede seguir
    data object Lista : ResultadoInicio

    // Ya tiene una solicitud activa para este perro: se bloquea
    data class Bloqueada(
        val solicitud: SolicitudResumen,
    ) : ResultadoInicio
}

// REGLA: una solicitud activa por usuario y perro.
// Si ya existe → bloquea. Si no → inicia el borrador en memoria (NO escribe en la BD).
class IniciarBorradorUseCase
@Inject
constructor(
    private val repository: SolicitudRepositoryInt,
    private val borrador: BorradorRepositoryInt,
) {
    suspend operator fun invoke(
        idUsuario: String,
        idPerro: String,
    ): ResultadoInicio {
        val activa = repository.getSolicitudActiva(idUsuario, idPerro) // solo LEE
        if (activa != null) return ResultadoInicio.Bloqueada(activa)

        borrador.iniciar(idUsuario, idPerro) // en memoria
        return ResultadoInicio.Lista
    }
}