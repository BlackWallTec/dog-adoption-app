package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.EdicionSolicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import javax.inject.Inject

class IniciarEdicionUseCase
    @Inject
    constructor(
        private val repository: SolicitudRepositoryInt,
        private val borrador: BorradorRepositoryInt,
    ) {
        suspend operator fun invoke(
            idUsuario: String,
            idSolicitud: String,
        ) {
            val solicitud = repository.getSolicitudParaEditar(idSolicitud) // Lanza NoEditableException

            val datosGenerales = repository.getPerfil().copy(ocupacion = solicitud.ocupacion)

            val original =
                BorradorSolicitud(
                    idUsuario = idUsuario,
                    idPerro = solicitud.idPerro,
                    aceptoRequisitos = true,
                    datosGenerales = datosGenerales,
                    tuHogar = solicitud.tuHogar,
                    compromiso = solicitud.compromiso,
                )
            borrador.reemplazar(
                original.copy(
                    edicion =
                        EdicionSolicitud(
                            idSolicitud = idSolicitud,
                            original = original,
                            tieneIneEnServidor = solicitud.tieneIne,
                            tieneDomicilioEnServidor = solicitud.tieneDomicilio,
                        ),
                ),
            )
        }
    }
