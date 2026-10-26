package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface

@Composable
// 1. EL ÁTOMO: palomita + nombre del documento subido
fun DocumentoItem(
    nombreArchivo: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically, // icono y textos alineados al centro
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = "Documento Salida",
            tint = Secondary,
            modifier = modifier.size(20.dp), // tantito mas chico que el icono normal
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = nombreArchivo,
            color = InverseSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Documento item")
@Composable
fun DocumentoItemPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DocumentoItem(nombreArchivo = "INE_Sofia_Garcia.pdf")
        DocumentoItem(nombreArchivo = "Domicilio_Sofia_Garcia.pdf")
    }
}

