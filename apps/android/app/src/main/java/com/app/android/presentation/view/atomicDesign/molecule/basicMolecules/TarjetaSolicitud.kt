package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.EtiquetaEstado
import com.app.android.presentation.view.ui.theme.Error
import com.app.android.presentation.view.ui.theme.ErrorContainer
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.PrimaryContainer
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.SecondaryContainer
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh
import com.app.android.presentation.view.ui.theme.SurfaceContainerLow

// Cómo se ve cada estado en la tarjeta (solo visual, no es del domain).
// editable = si muestra el botón "Editar"
enum class EstadoTarjeta(
    val texto: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorContenido: Color,
    val editable: Boolean,
) {
    EN_REVISION("En revisión", Icons.Outlined.Schedule, SecondaryContainer, Secondary, true),
    DOCUMENTO_RECHAZADO(
        "Documento rechazado",
        Icons.Outlined.WarningAmber,
        Color(0xFFFFDDB0),
        Color(0xFF8A4F00),
        true,
    ),
    EN_PERIODO_PRUEBA("En periodo de prueba", Icons.Outlined.Pets, PrimaryContainer, Color(0xFF514100), false),
    APROBADA("Aprobada", Icons.Filled.Check, Color(0xFFDDF0E0), Color(0xFF2E6B3A), false),
    RECHAZADA("Rechazada", Icons.Filled.Close, ErrorContainer, Error, false),
}

// 1. LA MOLECULA: foto del perro + nombre + folio/fecha + estado + "Editar"
@Composable
fun TarjetaSolicitud(
    foto: Painter,
    nombre: String,
    folio: String, // "QA-0248"
    fecha: String, // "12 oct 2026"
    estado: EstadoTarjeta,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val forma = RoundedCornerShape(16.dp)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(forma)
                .background(SurfaceContainerLow)
                .border(width = 1.dp, color = SurfaceContainerHigh, shape = forma)
                .clickable(onClick = onClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = foto,
            contentDescription = "Foto de $nombre",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(width = 64.dp, height = 72.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )

        Spacer(modifier = Modifier.width(12.dp))

        // weight(1f) = ocupa el espacio que sobra y empuja Editar hasta a la derecha
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                color = InverseSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$folio · $fecha",
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            EtiquetaEstado(
                texto = estado.texto,
                icono = estado.icono,
                colorFondo = estado.colorFondo,
                colorContenido = estado.colorContenido,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        // Solo se puede editar si sigue en revision o le rechazaron un documento
        if (estado.editable) {
            TextButton(
                onClick = onEditar,
                colors = ButtonDefaults.textButtonColors(contentColor = Secondary),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Editar", fontSize = 14.sp)
            }
        }
    }
}

// 2. PREVISUALIZACION: todos los estados
@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Tarjeta solicitud")
@Composable
fun TarjetaSolicitudPreview() {
    Column(
        modifier = Modifier.background(Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EstadoTarjeta.entries.forEach { estado ->
            TarjetaSolicitud(
                foto = painterResource(R.drawable.perro_prueba),
                nombre = "Bruno",
                folio = "QA-0248",
                fecha = "12 oct 2026",
                estado = estado,
            )
        }
    }
}
