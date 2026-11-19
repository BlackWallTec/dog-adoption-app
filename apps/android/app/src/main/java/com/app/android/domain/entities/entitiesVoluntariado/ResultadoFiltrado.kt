package com.app.android.domain.entities.entitiesVoluntariado

sealed interface ResultadoFiltrado {
    data class Exito(
        val programas: List<Programa>,
    ) : ResultadoFiltrado

    data class Invalido (
        val errores: ErroresFiltrosProgramas,
    ) : ResultadoFiltrado
}

