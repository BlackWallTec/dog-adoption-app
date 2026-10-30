package com.app.android.domain.entities.entitiesVoluntariado

import java.time.Instant

data class Inscripcion(
    val programaId: String,
    val fechaInscripcion: Instant,
    val estado: String,
)