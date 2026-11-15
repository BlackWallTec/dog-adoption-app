package com.app.android.domain.useCases.useCasesVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.EroresFiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class FiltrarProgramasUseCase
    @Inject
    constructor(
        private val clock: Clock,
    ) {
        private val zonaRefugio = ZoneId.of("America/Mexico_City")

        fun validar(filtros: FiltrosProgramas): EroresFiltrosProgramas {
            val hoy = LocalDate.now(clock.withZone(zonaRefugio))

            val desde = filtros.fechaDesde
            val hasta = filtros.fechaHasta
            val desdeHora = filtros.horaDesde
            val hastaHora = filtros.horaHasta

            val errorFechaDesde =
                if (desde != null && desde.isBefore(hoy)) {
                    "La fecha desde no puede ser anterior a hoy"
                } else {
                    null
                }

            val errorFechaHasta =
                when {
                    hasta != null && hasta.isBefore(hoy) -> "La fecha hasta no puede ser anterior a hoy"
                    desde != null && hasta != null && hasta.isBefore(desde) -> "La fecha desde no puede ser posterior a la fecha hasta"
                    else -> null
                }

            val errorHoraHasta =
                if (
                    desdeHora != null &&
                    hastaHora != null &&
                    !desdeHora.isBefore(hastaHora)
                ) {
                    "La hora final no puede ser posterior a la inicial"
                } else {
                    null
                }

            return EroresFiltrosProgramas(
                fechaDesde = errorFechaDesde,
                fechaHasta = errorFechaHasta,
                horaHasta = errorHoraHasta,
            )
        }

        fun filtrarPorFecha(
            programas: List<Programa>,
            filtros: FiltrosProgramas,
        ): List<Programa> {
            val errores = validar(filtros)

            require(!errores.hayErorres) {
                "Los filtros no son válidos: $errores"
            }

            val ahora = Instant.now(clock)

            return programas
                .filter { programa ->
                    programa.estado == EstadoPrograma.DISPONIBLE &&
                        programa.fechaInicio.isAfter(ahora)
                }.filter { programa ->
                    val inicio =
                        programa.fechaInicio
                            .atZone(zonaRefugio)
                            .toLocalDate()

                    val fin =
                        programa.fechaFin
                            .atZone(zonaRefugio)
                            .toLocalDate()

                    val desde = filtros.fechaDesde
                    val hasta = filtros.fechaHasta

                    val cumpleDesde =
                        desde == null || !fin.isBefore(desde)
                    val cumpleHasta =
                        hasta == null || !inicio.isAfter(hasta)

                    cumpleDesde && cumpleHasta
                }.sortedBy { programa -> programa.fechaInicio }
        }
    }
