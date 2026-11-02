package com.app.android.domain.repoInterface.repoIntVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Resultado

interface InscripcionRepository {
    suspend fun inscribirse(idPrograma: String): Resultado<Inscripcion>
}