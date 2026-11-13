package com.app.android.presentation.util.utilSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.presentation.viewmodel.viewModelSolicitudes.SolicitudItem
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

// Código generado por IA para formatear la fecha desde la BD a algo entendible para el usuario
// Más que nada la duda que tenia era en donde poner este tipo de Mapper y el formato de fecha.
private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

fun SolicitudCliente.toItem(): SolicitudItem =
    SolicitudItem(
        idSolicitud = idSolicitud,
        nombrePerro = nombrePerro ?: "Mascota no disponible",
        fotoUrl = fotoUrl,
        folio = folio,
        fecha = formatearFecha(fechaSolicitud),
        estado = estado,
        documentoRechazado = documentoRechazado,
        puedeEditar = puedeEditar,
        comentarioAdmin = comentarioAdmin,
    )

// Formatear lo que esta guardado en la BD 2026-10-12T18:30:00Z a algo más entendible: 12 oct 2026
private fun formatearFecha(iso: String): String =
    try {
        val fecha =
            try {
                OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
            } catch (e: Exception) {
                LocalDate.parse(iso.take(10)) // por si se manda la fecha sin la zona horaria
            }
        "${fecha.dayOfMonth} ${MESES[fecha.monthValue - 1]} ${fecha.year}"
    } catch (e: Exception) {
        "" // sin fecha válida: la tarjeta muestra solo el folio
    }
