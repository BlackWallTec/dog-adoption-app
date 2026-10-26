package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// 1. LA MOLÉCULA: icono pequeño a la izquierda y texto a la derecha
@Composable
fun DatoPrograma(
    icono: ImageVector,
    texto: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp), // espacio entre icono y texto
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null, // el texto ya explica
            tint = InverseSurface,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = texto,
            color = InverseSurface,
            fontSize = 14.sp,
        )
    }
}

// 2. PREVISUALIZACIÓN: fecha, horario y ubicación
@Preview(showBackground = true, name = "Datos del programa")
@Composable
fun DatoProgramaPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DatoPrograma(icono = Icons.Outlined.CalendarMonth, texto = "Sáb. 10 oct. 2026")
        DatoPrograma(icono = Icons.Outlined.Schedule, texto = "09:00 – 11:00 h")
        DatoPrograma(icono = Icons.Outlined.LocationOn, texto = "Refugio Qariño · Querétaro")
    }
}
