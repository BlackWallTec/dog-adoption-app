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
 * - texto: String (Obligatorio) -> Texto descriptivo o párrafo.
 * - modifier: Modifier -> Modificadores de diseño.
 * - color: Color -> Color del texto (Por defecto: onSurfaceVariant para menor jerarquía).
 * - fontSize: TextUnit -> Tamaño de lectura (Por defecto: 14.sp).
 * - fontWeight: FontWeight -> Grosor (Por defecto: Normal).
 */
@Composable
fun TextoCuerpo(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        text = texto,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
    )
}

@Preview(showBackground = true, name = "Átomo: TextoCuerpo")
@Composable
fun TextoCuerpoPreview() {
    AndroidTheme {
        TextoCuerpo(
            texto = "¡Hola, soy Bruno! Me encantan los paseos y jugar con mi pelota.",
        )
    }
}
