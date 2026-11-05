package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer

// Nombre de cada paso, en orden (i(0) = paso 1)
val pasosSolicitud =
    listOf(
        "Requisitos",
        "Datos generales",
        "Tu hogar",
        "Compromiso",
        "Documentos",
        "Confirmar",
    )

// MOLECULE: barra de progreso (segmentada( + el texto de paso x de 6 - Nombre
@Composable
fun IndicadorPasos(
    pasoActual: Int, // es de 0-5 igual que la navbar
    modifier: Modifier = Modifier,
) {
    // SI alguién manda un numero fuera del rango, se encierra entre el 0 y el ultimo
    val paso = pasoActual.coerceIn(0,pasosSolicitud.lastIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Fila de la barra de segmentos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pasosSolicitud.forEachIndexed { index, _ ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f) // Todos iguales
                            .height (6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index <= paso) Secondary else SurfaceContainer,
                            ),
                )
            }
        }

        // texto + 1 pq indicie empieza en 0
        Text(
            text = "Paso ${paso + 1} de ${pasosSolicitud.size} · ${pasosSolicitud[paso]}",
            color = InverseSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, name = "Indicador de pasos (todos)")
@Composable
fun IndicadorPasosPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        pasosSolicitud.indices.forEach { paso ->
            IndicadorPasos(pasoActual = paso)
        }
    }
}