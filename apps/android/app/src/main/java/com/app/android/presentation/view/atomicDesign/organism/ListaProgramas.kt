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
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaPrograma
import com.app.android.presentation.view.ui.theme.AndroidTheme
import java.text.SimpleDateFormat
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
                estado = programa.estadoDelPrograma.trim(),
                fechaFin = "Fin: ${formatearFecha(programa.fechaFin)}",
            )
        }
    }
}

// "2026-12-11T12:17:08.611" -> "11 dic 2026" (si falla, muestra el texto original)
private fun formatearFecha(fecha: String): String =
    runCatching {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha.take(10))!!
        SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-MX")).format(entrada)
    }.getOrDefault(fecha)

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, widthDp = 360, name = "Lista de programas")
@Composable
private fun ListaProgramasPreview() {
    AndroidTheme {
        ListaProgramas(
            programas =
                listOf(
                    Programa("1", "Cuidado de Perros", "Bazar de Donativos", "Disponible", "2026-12-11T12:17:08.611", "2026-12-16T12:17:08.611"),
                    Programa("2", "Limpieza", "Jornada de limpieza", "Disponible", "2026-12-20T09:00:00", "2026-12-20T13:00:00"),
                ),
            modifier = Modifier.padding(16.dp),
        )
    }
}