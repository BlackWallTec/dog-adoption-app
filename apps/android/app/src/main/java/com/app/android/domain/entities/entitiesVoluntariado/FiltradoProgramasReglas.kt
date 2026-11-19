package com.app.android.domain.entities.entitiesVoluntariado


import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val ZonaRefugio = ZoneId.of("America/Mexico_City")

// Valida y filtra la lista de programas
fun filtrarSegunReglas(
    programas: List<Programa>,
    filtros: FiltrosProgramas,
    ahora: Instant,
): ResultadoFiltrado {
    val hoy = ahora.atZone(ZonaRefugio).toLocalDate()
    val errores = filtros.validar(hoy)

    if (errores.hayErrores) {
        return ResultadoFiltrado.Invalido(errores)
    }

    val resultados = programas
        .filter { programa ->
            programa.estado == EstadoPrograma.DISPONIBLE &&
                    programa.fechaInicio.isAfter(ahora)
        }
        .filter { programa ->
            programa.coincideConFechas(filtros)
        }
        .filter { programa ->
            programa.coincideConHoras(filtros)
        }
        .sortedBy { programa -> programa.fechaInicio }

    return ResultadoFiltrado.Exito(resultados)
}
    private fun FiltrosProgramas.validar(
        hoy: LocalDate,
    ): ErroresFiltrosProgramas {
        val errorFechaDesde =
            if(fechaDesde != null && fechaDesde.isBefore(hoy)) {
                "La fecha desde no puede ser anterior a hoy"
            } else {
                null
            }

        val errorFechaHasta =
            when {
                fechaHasta != null && fechaHasta.isBefore(hoy) ->
                    "La fecha hasta no puede ser anterior a hoy"
                fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde) ->
                    "La fecha desde no puede ser posterior a la fecha hasta"
                else -> null
            }

        val errorHoraHasta =
            if (
                horaDesde != null &&
                horaHasta != null &&
                !horaDesde.isBefore(horaHasta)
            ) {
                "La hora final debe ser posterior a la inicial"
            } else {
                null
            }

        return ErroresFiltrosProgramas(
            fechaDesde = errorFechaDesde,
            fechaHasta = errorFechaHasta,
            horaHasta = errorHoraHasta,
        )
    }

private fun Programa.coincideConFechas(
    filtros: FiltrosProgramas
): Boolean {
    val inicio = fechaInicio.atZone(ZonaRefugio).toLocalDate()
    val fin = fechaFin.atZone(ZonaRefugio).toLocalDate()

    val desde = filtros.fechaDesde
    val hasta = filtros.fechaHasta

    val cumpleDesde =
        desde == null || !fin.isBefore(desde)

    val cumpleHasta =
        hasta == null || !inicio.isAfter(hasta)

    return cumpleDesde && cumpleHasta
}

private fun Programa.coincideConHoras(
    filtros: FiltrosProgramas,
): Boolean {
    val inicio = fechaInicio.atZone(ZonaRefugio).toLocalTime()
    val fin = fechaFin.atZone(ZonaRefugio).toLocalTime()

    val desde = filtros.horaDesde
    val hasta = filtros.horaHasta

    val cumpleDesde =
        desde == null || fin.isAfter(desde)

    val cumpleHasta =
        hasta == null || inicio.isBefore(hasta)

    return cumpleDesde && cumpleHasta
}