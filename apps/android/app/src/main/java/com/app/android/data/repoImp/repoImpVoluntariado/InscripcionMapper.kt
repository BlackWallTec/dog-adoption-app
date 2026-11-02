package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.InscripcionDto
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import java.time.LocalDateTime
import java.time.ZoneId

private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")

fun InscripcionDto.toDomain(): Inscripcion =
    Inscripcion(
        programaId = idPrograma,
        fechaInscripcion = LocalDateTime.parse(fechaInscripcion).atZone(ZONA_REFUGIO).toInstant(),
        estado = estado.trim(),
    )