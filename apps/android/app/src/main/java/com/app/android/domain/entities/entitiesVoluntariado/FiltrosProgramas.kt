package com.app.android.domain.entities.entitiesVoluntariado

import java.time.LocalDate
import java.time.LocalTime

data class FiltrosProgramas(
    val fechaDesde: LocalDate? = null,
    val fechaHasta: LocalDate? = null,
    val horaDesde: LocalTime? = null,
    val horaHasta: LocalTime? = null,
)

data class EroresFiltrosProgramas(
    val fechaDesde: String? = null,
    val fechaHasta: String? = null,
    val horaDesde: String? = null,
    val horaHasta: String? = null,
) {
    val hayErorres: Boolean
        get() = fechaDesde != null ||
                fechaHasta != null ||
                horaDesde != null ||
                horaHasta != null
}
