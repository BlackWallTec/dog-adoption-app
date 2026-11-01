package com.app.android.domain.useCases.useCasesSolicitudes

import java.util.Calendar
import javax.inject.Inject

class CalcularEdadUseCase
    @Inject
    constructor() {
    // fechaNacimiento viene como 1995-03-15 desde BD
        operator fun invoke(
            fechaNacimiento: String?,
            hoy: Calendar = Calendar.getInstance(),
        ): Int? {
            if (fechaNacimiento.isNullOrBlank()) return null

            // se agarra solo "1995-03-15" y se parte  en "1995", "03", y "15"
            val partes = fechaNacimiento.take(10).split("-")
            if (partes.size != 3) return null

            val anio = partes[0].toIntOrNull() ?: return null
            val mes = partes[1].toIntOrNull() ?: return null
            val dia = partes[2].toIntOrNull() ?: return null

            val anioHoy = hoy.get(Calendar.YEAR)
            val mesHoy = hoy.get(Calendar.MONTH) + 1 // segun calendar cuenta los meses desde 0
            val diaHoy = hoy.get(Calendar.DAY_OF_MONTH)

            var edad = anioHoy - anio
            // Si todavía no llega su cumpleaños este año, tiene un año menos
            if (mesHoy < mes || (mesHoy == mes && diaHoy < dia)) {
                edad--
            }
            return edad
        }
    }

//que desmadre para la edad gadddamn