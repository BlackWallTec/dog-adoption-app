package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

// REGLA: lo que ya está en el borrador tiene prioridad (regresó con back o viene de Editar).
// Si el paso no se ha llenado, se precarga del perfil (solo LECTURA).
class GetDatosGeneralesUseCase
@Inject
constructor(
    private val repository: SolicitudRepositoryInt,
    private val borrador: BorradorRepositoryInt,
) {
    suspend operator fun invoke(): DatosGenerales {
        val actual =
            borrador.borrador.value
                ?: throw IllegalStateException("No hay borrador iniciado")

        return actual.datosGenerales // 1. lo que ya llenó
            ?: repository.getPerfil(actual.idUsuario) // 2. precarga del perfil
    }
}