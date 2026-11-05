package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.AndroidTheme

/*
 * PARÁMETROS CONFIGURABLES:
 * - texto: String (Obligatorio) -> Contenido del título.
 * - modifier: Modifier -> Para tamaños, márgenes o alineación externa.
 * - color: Color -> Color del texto (Por defecto: onSurface del tema).
 * - fontSize: TextUnit -> Tamaño de la fuente (Por defecto: 20.sp).
 * - fontWeight: FontWeight -> Grosor de la letra (Por defecto: Bold).
 */
@Composable
fun TextoTitulo(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Text(
        text = texto,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
    )
}

@Preview(showBackground = true, name = "Átomo: TextoTítulo")
@Composable
fun TextoTituloPreview() {
    AndroidTheme {
        TextoTitulo(texto = "Salud y cuidados")
    }
}
