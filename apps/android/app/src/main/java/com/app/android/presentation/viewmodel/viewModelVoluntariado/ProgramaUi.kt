package com.app.android.presentation.viewmodel.viewModelVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.entities.entitiesVoluntariado.lugaresOcupados
import com.app.android.domain.entities.entitiesVoluntariado.puedeCancelar
import com.app.android.domain.entities.entitiesVoluntariado.validarInscripcion
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Data de presentación de un programa: todo lo que la tarjeta muestra, ya listo.
// La View solo lo pinta; no calcula ni decide nada.
data class ProgramaUi(
    val id: String,
    val tipo: String,
    val titulo: String,
    val fecha: String,
    val horario: String,
    val ubicacion: String,
    val textoCupo: String, // ej. "8/15 lugares ocupados · 7 disponibles"
    val progresoCupo: Float, // de 0f a 1f
    val textoBoton: String,
    val botonHabilitado: Boolean,
    val accion: AccionPrograma = AccionPrograma.INSCRIBIRSE, // qué hace el botón al tocarlo
)

// Qué evento dispara el botón de la tarjeta. La View lo usa para elegir el evento y el estilo.
enum class AccionPrograma { INSCRIBIRSE, CANCELAR }

private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")
private val LOCALE_ES: Locale = Locale.forLanguageTag("es-MX")
private val FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE. d MMM. yyyy", LOCALE_ES).withZone(ZONA_REFUGIO)
private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES).withZone(ZONA_REFUGIO)
private const val UBICACION_POR_DEFECTO = "Refugio Qariño · Querétaro"

// Entity -> data de presentación. "ahora" lo pasa el ViewModel para que todas las tarjetas
// se evalúen con la misma hora (y para poder probarlo con una hora fija).
fun Programa.toUi(ahora: Instant): ProgramaUi {
    // HU37: inscrito y sin iniciar -> el botón cancela. Si no, el dominio decide si se puede inscribir.
    val accion = if (puedeCancelar(ahora)) AccionPrograma.CANCELAR else AccionPrograma.INSCRIBIRSE
    val (textoBoton, botonHabilitado) =
        if (accion == AccionPrograma.CANCELAR) "Cancelar" to true else botonDe(validarInscripcion(ahora))
    return ProgramaUi(
        id = id,
        tipo = tipo,
        titulo = nombre,
        fecha = FORMATO_FECHA.format(fechaInicio).replaceFirstChar { it.uppercase() },
        horario = "${FORMATO_HORA.format(fechaInicio)} – ${FORMATO_HORA.format(fechaFin)} h",
        ubicacion = ubicacion ?: UBICACION_POR_DEFECTO,
        textoCupo = cupoTexto(),
        progresoCupo = cupoProgreso(),
        textoBoton = textoBoton,
        botonHabilitado = botonHabilitado,
        accion = accion,
    )
}

// El motivo lo decide el dominio (validarInscripcion); aquí solo se traduce a texto del botón
private fun botonDe(motivo: VoluntariadoError?): Pair<String, Boolean> =
    when (motivo) {
        null -> "Inscribirme" to true
        VoluntariadoError.YaInscrito -> "Inscrito" to false
        VoluntariadoError.SinCupo -> "Sin lugares" to false
        VoluntariadoError.ProgramaIniciado -> "Ya comenzó" to false
        else -> "No disponible" to false
    }

// Sin límite: "Sin límite de lugares"
private fun Programa.cupoTexto(): String {
    val maximo = cupoMaximo ?: return "Sin límite de lugares"
    val ocupados = lugaresOcupados() ?: 0
    val disponibles = (maximo - ocupados).coerceAtLeast(0)
    return "$ocupados/$maximo lugares ocupados · $disponibles disponibles"
}

// Sin límite = barra vacía; cupo 0 = barra llena (no cabe nadie)
private fun Programa.cupoProgreso(): Float {
    val maximo = cupoMaximo ?: return 0f
    if (maximo <= 0) return 1f
    val ocupados = lugaresOcupados() ?: 0
    return (ocupados.toFloat() / maximo).coerceIn(0f, 1f)
}