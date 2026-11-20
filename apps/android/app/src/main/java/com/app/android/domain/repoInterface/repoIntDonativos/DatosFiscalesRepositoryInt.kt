package com.app.android.domain.repoInterface.repoIntDonativos

import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales

interface DatosFiscalesRepositoryInt {
    // No reciben idUsuario: el backend lo saca del token de la sesión
    suspend fun getDatosFiscales(): DatosFiscales

    suspend fun guardarDatosFiscales(datos: DatosFiscales)

    // Regresa la ruta donde quedó guardada
    suspend fun subirConstancia(archivo: ConstanciaArchivo): String
}
