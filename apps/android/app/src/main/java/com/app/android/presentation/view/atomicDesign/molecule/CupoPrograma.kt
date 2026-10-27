package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.BarraCapacidad
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// 1. LA MOLÉCULA: texto del cupo arriba y barra de capacidad abajo
@Composable
fun CupoPrograma(
    texto: String,
    progreso: Float, // de 0f a 1f
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = texto,
            color = InverseSurface.copy(alpha = 0.7f), // mismo color pero mas claro
            fontSize = 14.sp,
        )
        BarraCapacidad(progreso = progreso)
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Cupo del programa")
@Composable
fun CupoProgramaPreview() {
    CupoPrograma(
        texto = "8/15 lugares ocupados · 7 disponibles",
        progreso = 0.53f,
        modifier = Modifier.background(Surface).padding(16.dp),
    )
}
