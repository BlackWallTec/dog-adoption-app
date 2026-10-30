package com.app.android.domain.entities.entitiesVoluntariado

import java.time.Instant

//Programa de voluntariado. La comparto con Oscar

data class Programa(
    val id: String,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val fechaInicio: Instant,
    val fechaFin: Instant,
    val ubicacion: String?,
    val cupoMaximo: Int?, // null = sin límite
    val cupoDisponible: Int?, // null = sin límite
    val estado: EstadoPrograma,
    val usuarioInscrito: Boolean,
)

enum class EstadoPrograma {
    DISPONIBLE,
    NO_DISPONIBLE,
    ;

    companion object {
        fun desdeTexto(texto: String?): EstadoPrograma =
            if (texto?.trim().equals("Disponible", ignoreCase = true)) DISPONIBLE else NO_DISPONIBLE
    }
}