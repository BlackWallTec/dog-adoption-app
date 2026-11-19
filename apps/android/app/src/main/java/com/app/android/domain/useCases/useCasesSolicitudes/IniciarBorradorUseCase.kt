package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

sealed interface ResultadoInicio {
    data object Lista : ResultadoInicio

    data class Bloqueada(
        val solicitud: SolicitudResumen,
    ) : ResultadoInicio
}

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
        val activa = repository.getSolicitudActiva(idPerro)
        if (activa != null) return ResultadoInicio.Bloqueada(activa)

        borrador.iniciar(idUsuario, idPerro)
        return ResultadoInicio.Lista
    }
}
