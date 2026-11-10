package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dtos.dtosVoluntariado.InscripcionDto
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion

fun InscripcionDto.toDomain(): Inscripcion =
    Inscripcion(
        programaId = idPrograma,
        fechaInscripcion = fechaInscripcion.aInstant(),
        estado = estado.orEmpty().trim(),
    )
