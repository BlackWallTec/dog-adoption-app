package com.app.android.domain.repoInterface.repoIntDonativos

import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales

interface DatosFiscalesRepositoryInt {
    suspend fun getDatosFiscales(idUsuario: String): DatosFiscales

    suspend fun guardarDatosFiscales(
        idUsuario: String,
        datos: DatosFiscales,
    )

    // Regresa la ruta donde quedó guardada
    suspend fun subirConstancia(
        idUsuario: String,
        archivo: ConstanciaArchivo,
    ): String
}
