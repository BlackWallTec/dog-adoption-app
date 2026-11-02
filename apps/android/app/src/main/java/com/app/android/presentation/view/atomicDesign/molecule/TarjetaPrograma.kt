package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.EtiquetaTipo
import com.app.android.presentation.view.ui.theme.AndroidTheme

// 1. LA MOLÉCULA: un programa de voluntariado
// nombre grande arriba; abajo una cuadrícula de 2x2:
// tipo (arriba izq) | fecha inicio (arriba der)
// estado (abajo izq) | fecha fin (abajo der)
@Composable
fun TarjetaPrograma(
    nombre: String,
    tipo: String,
    fechaInicio: String,
    estado: String,
    fechaFin: String,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = colores.surfaceContainer, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = nombre,
            color = colores.inverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EtiquetaTipo(texto = tipo)
            TextoSecundario(texto = fechaInicio)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextoSecundario(texto = estado)
            TextoSecundario(texto = fechaFin)
        }
    }
}

// Texto simple y pequeño para los datos secundarios
@Composable
private fun TextoSecundario(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.7f),
        fontSize = 14.sp,
    )
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, widthDp = 328, name = "Tarjeta programa")
@Composable
private fun TarjetaProgramaPreview() {
    AndroidTheme {
        TarjetaPrograma(
            nombre = "Bazar de Donativos",
            tipo = "Cuidado de Perros",
            fechaInicio = "Inicio: 11 dic 2026",
            estado = "Disponible",
            fechaFin = "Fin: 16 dic 2026",
            modifier = Modifier.padding(16.dp),
        )
    }
}