package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.SecondaryContainer
import com.app.android.presentation.view.ui.theme.Surface

// 1. El ATOMO: etiqueta de color con icono + texto (por ejemplo: En revisiion)
@Composable
fun EtiquetaEstado(
    texto: String,
    icono: ImageVector,
    colorFondo: Color,
    colorContenido: Color, // color del icono y del texto
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(color= colorFondo, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null, //el texto lo explica
            tint = colorContenido,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            color = colorContenido,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// 2. PREVISUALIZACION
@Preview(showBackground = true, name = "Etiqueta estado")
@Composable
fun EtiquetaEstadoPreview() {
    Column(
        modifier = Modifier.background(Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EtiquetaEstado(
            texto = "En revisión",
            icono = Icons.Outlined.Schedule,
            colorFondo = SecondaryContainer,
            colorContenido = Secondary,
        )
    }
}
