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
        cupoDisponible = cupoDisponible?.let { if (it <= 0) 0 else it - 1 },
    )

// HU37: el usuario puede cancelar solo si está inscrito y el programa todavía no inicia.
fun Programa.puedeCancelar(ahora: Instant): Boolean = usuarioInscrito && ahora.isBefore(fechaInicio)

// programa refleja una cancelación exitosa: libera el lugar (sin pasar de cupoMaximo).
fun Programa.conInscripcionCancelada(): Programa =
    copy(
        usuarioInscrito = false,
        cupoDisponible =
            cupoDisponible?.let { disponibles ->
                val liberado = disponibles + 1
                cupoMaximo?.let { liberado.coerceAtMost(it) } ?: liberado
            },
    )

// Lugares ya ocupados (cupoMaximo - cupoDisponible), entre 0 y cupoMaximo. null = programa sin límite.
fun Programa.lugaresOcupados(): Int? {
    val maximo = cupoMaximo ?: return null
    val disponibles = cupoDisponible ?: maximo
    return (maximo - disponibles).coerceIn(0, maximo.coerceAtLeast(0))
}