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
sealed interface AccionDetalle {
    data object Inscribirme : AccionDetalle

    data object YaInscrito : AccionDetalle

    data object CupoLleno : AccionDetalle

    data class SinAccion(
        val marca: MarcaPrograma,
    ) : AccionDetalle
}

enum class MarcaPrograma { CANCELADO, FINALIZADO, EN_CURSO, NO_DISPONIBLE }

// Regla del boton parte del diseño HU36. Reutiliza validarInscripcion de HU33 para no duplicar reglas
fun DetallePrograma.accion(ahora: Instant): AccionDetalle =
    when {
        cancelado -> {
            AccionDetalle.SinAccion(MarcaPrograma.CANCELADO)
        }

        finalizado(ahora) -> {
            AccionDetalle.SinAccion(MarcaPrograma.FINALIZADO)
        }

        else -> {
            when (programa.validarInscripcion(ahora)) {
                null -> AccionDetalle.Inscribirme
                VoluntariadoError.YaInscrito -> AccionDetalle.YaInscrito
                VoluntariadoError.SinCupo -> AccionDetalle.CupoLleno
                VoluntariadoError.ProgramaIniciado -> AccionDetalle.SinAccion(MarcaPrograma.EN_CURSO)
                else -> AccionDetalle.SinAccion(MarcaPrograma.NO_DISPONIBLE)
            }
        }
    }
