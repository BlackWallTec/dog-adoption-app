package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// 1. EL ÁTOMO: etiqueta gris arriba y valor en negrita abajo
@Composable
fun DatoResumen(
    etiqueta: String,
    valor: String?, // el ? permite que venga como null
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = etiqueta,
            color = InverseSurface.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )
        Text(
            text = valor ?: "Pendiente", // si no hay valor, enseña  "Pendiente"
            color = InverseSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// 2. PREVISUALIZACIÓN: uno con dato y uno sin
@Preview(showBackground = true, name = "Dato resumen")
@Composable
fun DatoResumenPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DatoResumen(
            etiqueta = "Edad",
            valor = "31 años",
        )
        DatoResumen(
            etiqueta = "Ocupación",
            valor = null, // muestra "Pendiente"
        )
    }
}