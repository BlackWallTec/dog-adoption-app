package com.app.android.domain.useCases.useCasesDonativos

import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import javax.inject.Inject

class RegistrarDonativoUseCase
    @Inject
    constructor(
        private val repository: DonativosRepositoryInt,
    ) {
        suspend operator fun invoke(
            idUsuario: String,
            monto: String,
            referencia: String,
            comprobante: ComprobanteArchivo,
        ) {
            // la referencia es opcional: si la dejó vacía no se manda
            repository.registrarDonativo(idUsuario, monto, referencia.trim().ifEmpty { null }, comprobante)
        }
    }
