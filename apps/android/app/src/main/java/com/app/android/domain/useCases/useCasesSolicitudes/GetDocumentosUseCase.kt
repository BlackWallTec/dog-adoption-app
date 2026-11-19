package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DocumentosSolicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

class GetDocumentosUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        // Continuar el borrador para poblar la información existente
        operator fun invoke(): DocumentosSolicitud {
            val actual =
                borrador.borrador.value ?: throw IllegalStateException("No hay borrador iniciado")
            return actual.documentos ?: DocumentosSolicitud()
        }
    }
