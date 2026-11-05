package com.app.android.presentation.viewmodel.viewModelVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError

data class InscripcionUiState(
    val idProgramaEnProceso: String? = null,
) {
    fun estaEnviando(idPrograma: String): Boolean = idProgramaEnProceso == idPrograma
}

sealed interface InscripcionEvento {
    data class Exitosa(val programaActualizado: Programa) : InscripcionEvento

    /** debeRecargar = true cuando el dato local quedó viejo y la pantalla debe pedirlo de nuevo. */
    data class Fallida(val mensaje: String, val debeRecargar: Boolean) : InscripcionEvento
}

fun VoluntariadoError.toMensaje(): String =
    when (this) {
        VoluntariadoError.SinCupo -> "Ya no hay lugares disponibles en este programa."
        VoluntariadoError.YaInscrito -> "Ya estás inscrito en este programa."
        VoluntariadoError.ProgramaIniciado -> "Este programa ya comenzó, ya no es posible inscribirse."
        VoluntariadoError.ProgramaNoDisponible -> "Este programa no está abierto a inscripciones."
        VoluntariadoError.ProgramaNoEncontrado -> "No encontramos este programa."
        VoluntariadoError.NoAutenticado -> "Necesitas iniciar sesión para inscribirte."
        VoluntariadoError.SinConexion -> "Sin conexión. Revisa tu internet e intenta otra vez."
        VoluntariadoError.Desconocido -> "Algo salió mal. Intenta más tarde."
    }

fun VoluntariadoError.requiereRecarga(): Boolean =
    this is VoluntariadoError.SinCupo ||
            this is VoluntariadoError.YaInscrito ||
            this is VoluntariadoError.ProgramaIniciado ||
            this is VoluntariadoError.ProgramaNoDisponible