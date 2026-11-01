package com.app.android.presentation.view.atomicDesign.atomic
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun HeatherSection(
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = titulo,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.inverseSurface
        )
        Text(
            text = descripcion,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            color = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EncabezadoSeccionPreview() {
    AndroidTheme {
        HeatherSection(
            titulo = "Documentos",
            descripcion = "Adjunta tus documentos. La información se usará únicamente para tu proceso de adopción.",
            modifier = Modifier.padding(16.dp)
        )
    }
}