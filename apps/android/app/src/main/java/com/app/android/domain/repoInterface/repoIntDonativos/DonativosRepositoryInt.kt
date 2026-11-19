package com.app.android.domain.repoInterface.repoIntDonativos

import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.entities.entitiesDonativos.Donativo

interface DonativosRepositoryInt {
    suspend fun registrarDonativo(
        idUsuario: String,
        monto: String,
        referencia: String?,
        comprobante: ComprobanteArchivo,
    )

    suspend fun getMisDonativos(): List<Donativo>

    suspend fun solicitarCfdi(idDonativo: String)
}
