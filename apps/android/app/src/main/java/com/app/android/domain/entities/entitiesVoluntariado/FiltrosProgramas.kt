package com.app.android.domain.entities.entitiesVoluntariado

import java.time.LocalDate
import java.time.LocalTime

// null define que no hay filtro.
data class FiltrosProgramas(
    val fechaDesde: LocalDate? = null,
    val fechaHasta: LocalDate? = null,
    val horaDesde: LocalTime? = null,
    val horaHasta: LocalTime? = null,
)

// Un mensaje por campo, si un campo tiene null significa que no hay error.
data class ErroresFiltrosProgramas(
    val fechaDesde: String? = null,
    val fechaHasta: String? = null,
    val horaDesde: String? = null,
    val horaHasta: String? = null,
) {
    val hayErrores: Boolean
        get() =
            fechaDesde != null ||
                fechaHasta != null ||
                horaDesde != null ||
                horaHasta != null
}
