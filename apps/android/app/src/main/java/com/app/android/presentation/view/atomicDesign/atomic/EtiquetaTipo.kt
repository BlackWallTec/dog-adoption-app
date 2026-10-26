package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.PrimaryContainer
import com.app.android.presentation.view.ui.theme.Surface

private val OnPrimaryContainer = Color(0xFF514100)

// 1. EL ÁTOMO: chip amarillo con el tipo (ej. "Paseo")
@Composable
fun EtiquetaTipo(
    texto: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        color = OnPrimaryContainer,
        fontSize = 14.sp,
        modifier =
            modifier
                .background(color = PrimaryContainer, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Etiqueta tipo")
@Composable
fun EtiquetaTipoPreview() {
    EtiquetaTipo(
        texto = "Paseo",
        modifier = Modifier.background(Surface).padding(16.dp),
    )
}
