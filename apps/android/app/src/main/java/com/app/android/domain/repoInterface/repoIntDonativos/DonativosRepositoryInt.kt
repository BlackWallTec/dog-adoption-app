package com.app.android.domain.repoInterface.repoIntDonativos

import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo

interface DonativosRepositoryInt {
    suspend fun registrarDonativo(
        idUsuario: String,
        monto: String,
        referencia: String?,
        comprobante: ComprobanteArchivo,
    )
}
