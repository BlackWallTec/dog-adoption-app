package com.app.android.presentation.view.viewsVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Textos de un Programa para la UI. Los usan el catálogo y la confirmación.
private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")
private val LOCALE_ES: Locale = Locale.forLanguageTag("es-MX")
private val FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE. d MMM. yyyy", LOCALE_ES).withZone(ZONA_REFUGIO)
private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES).withZone(ZONA_REFUGIO)
private const val UBICACION_POR_DEFECTO = "Refugio Qariño · Querétaro"

fun Programa.fechaTexto(): String =
    FORMATO_FECHA.format(fechaInicio).replaceFirstChar { it.uppercase() }

fun Programa.horarioTexto(): String =
    "${FORMATO_HORA.format(fechaInicio)} – ${FORMATO_HORA.format(fechaFin)} h"

fun Programa.ubicacionTexto(): String = ubicacion ?: UBICACION_POR_DEFECTO