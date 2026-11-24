package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError

/*
 * SHARED TOOLBOX FOR THE HU37 STRESS TESTS
 *
 * These helpers sit next to `Herramientas.kt` (the project's existing toolbox, which gives us
 * AHORA, RELOJ_FIJO, programaValido(), INSCRIPCION_OK and RepositorioFalso).
 * Nothing here is a test: it is only shared code so each test file stays short.
 */

/*
 * ALL the errors the app can produce, including `NoInscrito` (the one HU37 introduced).
 *
 * Why not reuse `TODOS_LOS_ERRORES` from Herramientas.kt? Because that list was written
 * before HU37 and does not contain NoInscrito, so loops over it would silently skip it.
 */
val ERRORES_DE_VOLUNTARIADO: List<VoluntariadoError> =
    listOf(
        VoluntariadoError.SinCupo,
        VoluntariadoError.YaInscrito,
        VoluntariadoError.NoInscrito,
        VoluntariadoError.ProgramaIniciado,
        VoluntariadoError.ProgramaNoDisponible,
        VoluntariadoError.ProgramaNoEncontrado,
        VoluntariadoError.NoAutenticado,
        VoluntariadoError.SinConexion,
        VoluntariadoError.Desconocido,
    )

/*
 * A SAFETY NET AGAINST FORGETTING THINGS (a trick worth learning).
 *
 * A `when` that is used as an expression over a sealed type MUST list every case, or the
 * code does not compile. So the day somebody adds a 10th error to VoluntariadoError, this
 * function stops compiling and tells you: "update ERRORES_DE_VOLUNTARIADO and the tests".
 * The function is never called; its only job is to break the build at the right moment.
 */
@Suppress("unused")
private fun recordatorioSiAparecenNuevosErrores(error: VoluntariadoError): Int =
    when (error) {
        VoluntariadoError.SinCupo,
        VoluntariadoError.YaInscrito,
        VoluntariadoError.NoInscrito,
        VoluntariadoError.ProgramaIniciado,
        VoluntariadoError.ProgramaNoDisponible,
        VoluntariadoError.ProgramaNoEncontrado,
        VoluntariadoError.NoAutenticado,
        VoluntariadoError.SinConexion,
        VoluntariadoError.Desconocido,
            -> 0
    }

/*
 * A program the user IS enrolled in and that starts in the future: the only kind of program
 * that can be cancelled. Tests start from this and change one detail at a time.
 */
fun programaInscrito(
    id: String = "programa-1",
    nombre: String = "Paseo de perritos",
    cupoDisponible: Int? = 5,
    cupoMaximo: Int? = 10,
): Programa =
    programaValido().copy(
        id = id,
        nombre = nombre,
        usuarioInscrito = true,
        cupoDisponible = cupoDisponible,
        cupoMaximo = cupoMaximo,
    )