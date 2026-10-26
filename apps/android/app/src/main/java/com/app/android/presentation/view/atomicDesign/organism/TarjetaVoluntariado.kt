package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.PrimaryContainer
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer

// Texto café oscuro sobre el chip amarillo (no existe en el tema)
private val OnPrimaryContainer = Color(0xFF514100)

// 1. EL ORGANISMO: tarjeta con la info de un programa de voluntariado
@Composable
fun TarjetaVoluntariado(
    tipo: String,
    titulo: String,
    fecha: String,
    horario: String,
    ubicacion: String,
    lugaresOcupados: Int,
    lugaresTotales: Int,
    alVerDetalle: () -> Unit,
    alInscribirse: () -> Unit,
    modifier: Modifier = Modifier,
    textoBoton: String = "Inscribirme",
) {
    val disponibles = (lugaresTotales - lugaresOcupados).coerceAtLeast(0)
    val progreso = if (lugaresTotales > 0) lugaresOcupados.toFloat() / lugaresTotales else 0f

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = SurfaceContainer, shape = RoundedCornerShape(24.dp))
                .clickable(onClick = alVerDetalle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header: chip del tipo a la izq, flecha a la derecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tipo,
                color = OnPrimaryContainer,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .background(color = PrimaryContainer, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
            )

            Spacer(modifier = Modifier.weight(1f)) // Empuja la flecha hasta la derecha

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = Secondary,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text = titulo,
            color = InverseSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )

        DatoPrograma(icono = Icons.Outlined.CalendarMonth, texto = fecha)
        DatoPrograma(icono = Icons.Outlined.Schedule, texto = horario)
        DatoPrograma(icono = Icons.Outlined.LocationOn, texto = ubicacion)

        // Cupo: texto y barra de capacidad
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "$lugaresOcupados/$lugaresTotales lugares ocupados · $disponibles disponibles",
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(color = InverseSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(999.dp)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progreso.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(color = Secondary, shape = RoundedCornerShape(999.dp)),
                )
            }
        }

        AppButton(
            text = textoBoton,
            onClick = alInscribirse,
            variant = ButtonVariant.Primary,
            enabled = disponibles > 0,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// Renglón con icono pequeño y texto (fecha, horario, ubicación)
@Composable
private fun DatoPrograma(
    icono: ImageVector,
    texto: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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

// 2. PREVISUALIZACIONES: con lugares y sin lugares
@Preview(showBackground = true, widthDp = 328, name = "Tarjeta de voluntariado")
@Composable
fun TarjetaVoluntariadoPreview() {
    AndroidTheme {
        Column(
            modifier =
                Modifier
                    .background(Surface)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TarjetaVoluntariado(
                tipo = "Paseo",
                titulo = "Paseos con cariño",
                fecha = "Sáb. 10 oct. 2026",
                horario = "09:00 – 11:00 h",
                ubicacion = "Refugio Qariño · Querétaro",
                lugaresOcupados = 8,
                lugaresTotales = 15,
                alVerDetalle = {},
                alInscribirse = {},
            )
            TarjetaVoluntariado(
                tipo = "Limpieza",
                titulo = "Jornada de limpieza",
                fecha = "Dom. 11 oct. 2026",
                horario = "10:00 – 13:00 h",
                ubicacion = "Refugio Qariño · Querétaro",
                lugaresOcupados = 10,
                lugaresTotales = 10,
                alVerDetalle = {},
                alInscribirse = {},
            )
        }
    }
}
