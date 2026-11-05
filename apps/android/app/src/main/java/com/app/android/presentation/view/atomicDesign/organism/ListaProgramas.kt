package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaVoluntariado
import com.app.android.presentation.view.ui.theme.AndroidTheme
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ListaProgramas(
    programas: List<Programa>,
    onInscribirse: (Programa) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = programas,
            key = { it.id },
        ) { programa ->
            val zoneId = ZoneId.systemDefault()
            val dateFormatter = DateTimeFormatter.ofPattern("EEE. d MMM. yyyy", Locale("es", "ES"))
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

            val fechaTexto = dateFormatter.withZone(zoneId).format(programa.fechaInicio).replaceFirstChar { it.uppercase() }
            val horaInicio = timeFormatter.withZone(zoneId).format(programa.fechaInicio)
            val horaFin = timeFormatter.withZone(zoneId).format(programa.fechaFin)
            val horarioTexto = "$horaInicio – $horaFin h"

            val (textoBoton, botonHabilitado) = botonDe(programa)

            TarjetaVoluntariado(
                tipo = programa.tipo,
                titulo = programa.nombre,
                fecha = fechaTexto,
                horario = horarioTexto,
                ubicacion = programa.ubicacion ?: "Refugio Qariño · Querétaro",
                textoCupo = "",
                progresoCupo = 0f,
                alAccionBoton = { onInscribirse(programa) },
                textoBoton = textoBoton,
                botonHabilitado = botonHabilitado,
            )
        }
    }
}

// El botón siempre se muestra; su texto y si se puede tocar dependen del estado del programa
private fun botonDe(programa: Programa): Pair<String, Boolean> =
    when {
        programa.usuarioInscrito -> "Inscrito" to false
        programa.estado != EstadoPrograma.DISPONIBLE -> "No disponible" to false
        else -> "Inscribirme" to true
    }

private fun programaDePrueba(
    id: String,
    nombre: String,
    tipo: String,
    inicio: String,
    fin: String,
    estado: EstadoPrograma = EstadoPrograma.DISPONIBLE,
) = Programa(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = null,
    fechaInicio = LocalDateTime.parse(inicio).toInstant(ZoneOffset.UTC),
    fechaFin = LocalDateTime.parse(fin).toInstant(ZoneOffset.UTC),
    ubicacion = null,
    cupoMaximo = null,
    cupoDisponible = null,
    estado = estado,
    usuarioInscrito = false,
)

@Preview(showBackground = true, widthDp = 360, name = "Lista de programas")
@Composable
private fun ListaProgramasPreview() {
    AndroidTheme {
        ListaProgramas(
            programas =
                listOf(
                    programaDePrueba("1", "Bazar de Donativos", "Cuidado de Perros", "2026-12-11T12:17:08", "2026-12-16T12:17:08"),
                    programaDePrueba("2", "Jornada de limpieza", "Limpieza", "2026-12-20T09:00:00", "2026-12-20T13:00:00"),
                ),
            onInscribirse = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}