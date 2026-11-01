package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.EtiquetaTipo
import com.app.android.presentation.view.atomicDesign.molecule.CupoPrograma
import com.app.android.presentation.view.atomicDesign.molecule.DatoPrograma
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer

// 1. EL ORGANISMO: tarjeta con la info de un programa de voluntariado
@Composable
fun TarjetaVoluntariado(
    tipo: String,
    titulo: String,
    fecha: String,
    horario: String,
    ubicacion: String,
    textoCupo: String, // ya viene armado, ej. "8/15 lugares ocupados · 7 disponibles"
    progresoCupo: Float, // de 0f a 1f
    alVerDetalle: (() -> Unit)? = null,
    alAccionBoton: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    textoBoton: String = "Inscribirme",
    varianteBoton: ButtonVariant = ButtonVariant.Primary,
    botonHabilitado: Boolean = true,
) {
    val modifierClickeable =
        if (alVerDetalle != null) {
            modifier.clickable(onClick = alVerDetalle)
        } else {
            modifier
        }

    Column(
        modifier =
            modifierClickeable
                .fillMaxWidth()
                .background(color = SurfaceContainer, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header: chip del tipo a la izq, flecha a la derecha solo si es navegable
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EtiquetaTipo(texto = tipo)

            // Flecha visible solo si se define alVerDetalle
            if (alVerDetalle != null) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalle",
                    tint = Secondary,
                    modifier = Modifier.size(18.dp),
                )
            }
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

        CupoPrograma(texto = textoCupo, progreso = progresoCupo)

        // Renderizar botón SOLO si se pasa la función alAccionBoton
        if (alAccionBoton != null) {
            AppButton(
                text = textoBoton,
                onClick = alAccionBoton,
                variant = varianteBoton,
                enabled = botonHabilitado,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
                textoCupo = "8/15 lugares ocupados · 7 disponibles",
                progresoCupo = 0.53f,
                alVerDetalle = {},
                alAccionBoton = {},
            )
            TarjetaVoluntariado(
                tipo = "Limpieza",
                titulo = "Jornada de limpieza",
                fecha = "Dom. 11 oct. 2026",
                horario = "10:00 – 13:00 h",
                ubicacion = "Refugio Qariño · Querétaro",
                textoCupo = "10/10 lugares ocupados · 0 disponibles",
                progresoCupo = 1f,
                alVerDetalle = {},
                alAccionBoton = {},
                botonHabilitado = false,
            )
        }
    }
}