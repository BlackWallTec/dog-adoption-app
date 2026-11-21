package com.app.android.domain.repoInterface.repoIntDonativos

import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.entities.entitiesDonativos.Donativo

interface DonativosRepositoryInt {
    // El usuario sale del token, igual que en mis donativos
    suspend fun registrarDonativo(
        monto: String,
        referencia: String?,
        comprobante: ComprobanteArchivo,
    )

    suspend fun getMisDonativos(): List<Donativo>

    suspend fun solicitarCfdi(idDonativo: String)
}
