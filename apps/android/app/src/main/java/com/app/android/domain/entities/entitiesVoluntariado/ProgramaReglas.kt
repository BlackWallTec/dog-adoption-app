package com.app.android.domain.entities.entitiesVoluntariado

import java.time.Instant

// Regresa el motivo por el que NO se puede inscribir, o null si sí se puede.
fun Programa.validarInscripcion(ahora: Instant): VoluntariadoError? =
    when {
        usuarioInscrito -> VoluntariadoError.YaInscrito
        estado != EstadoPrograma.DISPONIBLE -> VoluntariadoError.ProgramaNoDisponible
        !ahora.isBefore(fechaInicio) -> VoluntariadoError.ProgramaIniciado
        cupoDisponible != null && cupoDisponible <= 0 -> VoluntariadoError.SinCupo
        else -> null
    }

// programa refleja una inscripción exitosa del usuario.

fun Programa.conInscripcionConfirmada(): Programa =
    copy(
        usuarioInscrito = true,
        cupoDisponible = cupoDisponible?.minus(1)?.coerceAtLeast(0),
    )