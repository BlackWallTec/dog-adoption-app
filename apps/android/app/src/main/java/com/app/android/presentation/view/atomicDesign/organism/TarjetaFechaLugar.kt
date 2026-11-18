package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.molecule.CupoPrograma
import com.app.android.presentation.view.atomicDesign.molecule.DatoPrograma
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer

// 1. EL ORGANISMO: tarjeta "Fecha y lugar" del detalle de un programa (HU36)
@Composable
fun TarjetaFechaLugar(
    fecha: String,
    horaInicio: String,
    horaFin: String,
    ubicacion: String,
    textoCupo: String,
    progresoCupo: Float, // de 0f a 1f
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = SurfaceContainer, shape = RoundedCornerShape(24.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Fecha y lugar",
            color = InverseSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        DatoPrograma(icono = Icons.Outlined.CalendarMonth, texto = fecha)

        Row(modifier = Modifier.fillMaxWidth()) {
            HoraEtiquetada(etiqueta = "Hora de inicio", hora = horaInicio, modifier = Modifier.weight(1f))
            HoraEtiquetada(etiqueta = "Hora de fin", hora = horaFin, modifier = Modifier.weight(1f))
        }

        DatoPrograma(icono = Icons.Outlined.LocationOn, texto = ubicacion)

        CupoPrograma(texto = textoCupo, progreso = progresoCupo)
    }
}

@Composable
private fun HoraEtiquetada(
    etiqueta: String,
    hora: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = etiqueta, color = InverseSurface.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(text = hora, color = InverseSurface, fontSize = 16.sp)
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, widthDp = 328, name = "Tarjeta fecha y lugar")
@Composable
fun TarjetaFechaLugarPreview() {
    AndroidTheme {
        TarjetaFechaLugar(
            fecha = "Sáb. 10 oct. 2026",
            horaInicio = "09:00",
            horaFin = "11:00",
            ubicacion = "Refugio Qariño · Querétaro",
            textoCupo = "8/15 lugares ocupados · 7 disponibles",
            progresoCupo = 0.53f,
            modifier = Modifier.background(Surface).padding(16.dp),
        )
    }
}