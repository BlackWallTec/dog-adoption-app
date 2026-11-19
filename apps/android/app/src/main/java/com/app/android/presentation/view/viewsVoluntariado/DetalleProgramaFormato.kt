package com.app.android.presentation.view.viewsVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.MarcaPrograma
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// HU36: textos del detalle. Mismo formato que ProgramasFormato.kt (catálogo y confirmación).
private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")
private val LOCALE_ES: Locale = Locale.forLanguageTag("es-MX")
private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES).withZone(ZONA_REFUGIO)

// Un día: "Sáb. 10 oct. 2026". Varios días: "Dom. 15 nov. 2026 – Dom. 20 dic. 2026"
fun DetallePrograma.fechaDetalleTexto(): String {
    val inicio = programa.fechaInicio.atZone(ZONA_REFUGIO).toLocalDate()
    val fin = programa.fechaFin.atZone(ZONA_REFUGIO).toLocalDate()
    val textoInicio = programa.fechaTexto()
    return if (inicio == fin) textoInicio else "$textoInicio – ${programa.copy(fechaInicio = programa.fechaFin).fechaTexto()}"
}

fun DetallePrograma.horaInicioTexto(): String = FORMATO_HORA.format(programa.fechaInicio)

fun DetallePrograma.horaFinTexto(): String = FORMATO_HORA.format(programa.fechaFin)

fun DetallePrograma.cupoTexto(): String {
    val cupo = programa.cupoMaximo ?: return "$inscritos inscritos · Sin límite de lugares"
    val disponibles = (cupo - inscritos).coerceAtLeast(0)
    return "$inscritos/$cupo lugares ocupados · $disponibles disponibles"
}

fun DetallePrograma.progresoCupo(): Float {
    val cupo = programa.cupoMaximo ?: return 0f
    return if (cupo <= 0) 0f else (inscritos.toFloat() / cupo).coerceIn(0f, 1f)
}

fun MarcaPrograma.texto(): String =
    when (this) {
        MarcaPrograma.CANCELADO -> "Cancelado"
        MarcaPrograma.FINALIZADO -> "Finalizado"
        MarcaPrograma.EN_CURSO -> "En curso"
        MarcaPrograma.NO_DISPONIBLE -> "No disponible"
    }
