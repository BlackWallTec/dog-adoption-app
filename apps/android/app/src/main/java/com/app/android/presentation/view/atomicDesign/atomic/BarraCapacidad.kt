package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface

// 1. EL ÁTOMO: barra gris con la parte ocupada en azul
@Composable
fun BarraCapacidad(
    progreso: Float, // de 0f a 1f
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(999.dp) // completamente redonda

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(color = InverseSurface.copy(alpha = 0.1f), shape = forma),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progreso)
                    .fillMaxHeight()
                    .background(color = Secondary, shape = forma),
        )
    }
}

// 2. PREVISUALIZACIÓN: vacía, a la mitad y llena
@Preview(showBackground = true, name = "Barra capacidad")
@Composable
fun BarraCapacidadPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BarraCapacidad(progreso = 0f)
        BarraCapacidad(progreso = 0.53f)
        BarraCapacidad(progreso = 1f)
    }
}
