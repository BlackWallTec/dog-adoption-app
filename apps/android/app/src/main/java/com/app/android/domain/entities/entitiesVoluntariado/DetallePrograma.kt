package com.app.android.domain.entities.entitiesVoluntariado

import java.time.Instant

// HU36 detalle de un programa, envuelve a Programa sin modificarla
data class DetallePrograma(
    val programa: Programa,
    val tipoDescripcion: String?,
    val inscritos: Int,
    val estadoGuardado: String?,
) {
    val cancelado: Boolean
        get() = estadoGuardado.equals("Cancelado", ignoreCase = true)

    fun finalizado(ahora: Instant): Boolean = estadoGuardado.equals("Finalizado", ignoreCase = true) || !ahora.isBefore(programa.fechaFin)
}

// Que muestra la pantalla en lugar del boton
sealed interface AccionPrograma {
    data object Inscribirme : AccionPrograma

    data object YaInscrito : AccionPrograma

    data object CupoLleno : AccionPrograma

    data class SinAccion(
        val marca: MarcaPrograma,
    ) : AccionPrograma
}

enum class MarcaPrograma { CANCELADO, FINALIZADO, EN_CURSO, NO_DISPONIBLE }

// Regla del boton parte del diseño HU36. Reutiliza validarInscripcion de HU33 para no duplicar reglas
fun DetallePrograma.accion(ahora: Instant): AccionPrograma =
    when {
        cancelado -> {
            AccionPrograma.SinAccion(MarcaPrograma.CANCELADO)
        }

        finalizado(ahora) -> {
            AccionPrograma.SinAccion(MarcaPrograma.FINALIZADO)
        }

        else -> {
            when (programa.validarInscripcion(ahora)) {
                null -> AccionPrograma.Inscribirme
                VoluntariadoError.YaInscrito -> AccionPrograma.YaInscrito
                VoluntariadoError.SinCupo -> AccionPrograma.CupoLleno
                VoluntariadoError.ProgramaIniciado -> AccionPrograma.SinAccion(MarcaPrograma.EN_CURSO)
                else -> AccionPrograma.SinAccion(MarcaPrograma.NO_DISPONIBLE)
            }
        }
    }
