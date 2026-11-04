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
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaPrograma
import com.app.android.presentation.view.ui.theme.AndroidTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// 1. EL ORGANISMO: la lista de programas vigentes
@Composable
fun ListaProgramas(
    programas: List<Programa>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(programas, key = { it.id }) { programa ->
            TarjetaPrograma(
                nombre = programa.nombre,
                tipo = programa.tipo,
                fechaInicio = "Inicio: ${formatearFecha(programa.fechaInicio)}",
                estado = textoEstado(programa.estado),
                fechaFin = "Fin: ${formatearFecha(programa.fechaFin)}",
            )
        }
    }
}

// Se formatea en UTC para mostrar la fecha tal como está guardada (sin correrla por zona horaria)
private val formatoFecha: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-MX")).withZone(ZoneOffset.UTC)

private fun formatearFecha(fecha: Instant): String = formatoFecha.format(fecha)

private fun textoEstado(estado: EstadoPrograma): String =
    if (estado == EstadoPrograma.DISPONIBLE) "Disponible" else "No disponible"

// 2. PREVISUALIZACIÓN
private fun programaDePrueba(
    id: String,
    nombre: String,
    tipo: String,
    inicio: String,
    fin: String,
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
    estado = EstadoPrograma.DISPONIBLE,
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
            modifier = Modifier.padding(16.dp),
        )
    }
}