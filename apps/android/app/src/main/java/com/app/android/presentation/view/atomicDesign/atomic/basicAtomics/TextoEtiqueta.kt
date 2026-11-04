package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

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
 * - texto: String (Obligatorio) -> Texto conciso para badges, chips o métricas.
 * - modifier: Modifier -> Modificadores externos.
 * - color: Color -> Color del texto (Por defecto: Unspecified para heredar del contenedor).
 * - fontSize: TextUnit -> Tamaño reducido (Por defecto: 12.sp).
 * - fontWeight: FontWeight -> Grosor (Por defecto: Medium).
 */
@Composable
fun TextoEtiqueta(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Medium,
) {
    Text(
        text = texto,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
    )
}

@Preview(showBackground = true, name = "Átomo: TextoEtiqueta")
@Composable
fun TextoEtiquetaPreview() {
    AndroidTheme { TextoEtiqueta(texto = "Vacunado") }
}
