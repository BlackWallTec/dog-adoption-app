package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme

/*
 * PARÁMETROS CONFIGURABLES (Aplica Caso 1: Contenedor Simple de Slot API):
 * - modifier: Modifier -> Modificador del contenedor base.
 * - colorFondo: Color -> Color del fondo (Por defecto: surfaceVariant del tema).
 * - forma: Shape -> Forma del recorte (Por defecto: RoundedCornerShape de 16.dp).
 * - paddingHorizontal: Dp -> Margen interno horizontal (Por defecto: 12.dp).
 * - paddingVertical: Dp -> Margen interno vertical (Por defecto: 6.dp).
 * - content: @Composable () -> Unit -> Slot hijo para renderizar cualquier contenido interno.
 */
@Composable
fun ChipSuperficie(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorFondo: Color = MaterialTheme.colorScheme.surfaceVariant,
    forma: Shape = RoundedCornerShape(16.dp),
    paddingHorizontal: Dp = 12.dp,
    paddingVertical: Dp = 6.dp,
    content: @Composable () -> Unit,
) {
    if (!enabled) return
    Box(
        modifier =
            modifier
                .background(color = colorFondo, shape = forma)
                .padding(horizontal = paddingHorizontal, vertical = paddingVertical),
    ) {
        content()
    }
}

@Preview(showBackground = true, name = "Átomo: ChipSuperficie")
@Composable
fun ChipSuperficiePreview() {
    AndroidTheme {
        ChipSuperficie {
            TextoEtiqueta(texto = "Ejemplo Chip")
        }
    }
}
