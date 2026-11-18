package com.app.android.domain.repoInterface.repoIntVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.Resultado

// HU36
interface DetalleProgramaRepository {
    suspend fun obtenerDetalle(idPrograma: String): Resultado<DetallePrograma>
}
