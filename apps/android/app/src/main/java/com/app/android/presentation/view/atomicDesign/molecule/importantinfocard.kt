package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Primary
import com.app.android.presentation.view.ui.theme.Surface

// 1. LA MOLÉCULA: tarjeta amarilla para avisos importantes
@Composable
fun ImportantInfoCard(
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier,
) {
    // Column = titulo arriba, descripción abajo
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Primary, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
    ) {
        Text(
            text = titulo,
            color = InverseSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = descripcion,
            color = InverseSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Aviso importante")
@Composable
fun ImportantInfoCardPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        ImportantInfoCard(
            titulo = "IMPORTANTE: La esterilización es requisito.",
            descripcion = "Los adultos se entregan esterilizados; si es cachorro se firma una carta compromiso para esterilizarlo a los 4 meses.",
        )
    }
}