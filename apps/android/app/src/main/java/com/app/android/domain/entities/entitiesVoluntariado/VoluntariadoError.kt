package com.app.android.domain.entities.entitiesVoluntariado

sealed interface VoluntariadoError {
    data object SinCupo : VoluntariadoError

    data object YaInscrito : VoluntariadoError

    data object ProgramaIniciado : VoluntariadoError

    data object ProgramaNoDisponible : VoluntariadoError

    data object ProgramaNoEncontrado : VoluntariadoError

    data object NoAutenticado : VoluntariadoError

    data object SinConexion : VoluntariadoError

    data object Desconocido : VoluntariadoError
}