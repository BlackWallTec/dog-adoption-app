package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.ChipSuperficie
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.IconoEstado
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoEtiqueta

enum class IconPosition { START, END }

/*
 * PARÁMETROS CONFIGURABLES:
 * - texto: String (Obligatorio) -> Texto a mostrar.
 * - modifier: Modifier -> Modificadores externos.
 * - icono: ImageVector? = null -> Ícono opcional. Si es null, no se dibuja.
 * - iconPosition: IconPosition -> Posición del ícono respecto al texto (START o END).
 * - conChip: Boolean -> Si es true, añade el contenedor con fondo y padding. Si es false, queda plano.
 * - espaciado: Dp -> Separación horizontal entre ícono y texto.
 * - colorFondo: Color -> Color del fondo del chip (cuando conChip = true)[cite: 6].
 * - colorTexto: Color -> Color del texto interno.
 * - tintIcono: Color -> Color del ícono (por defecto igual al color del texto).
 * - forma: Shape -> Recorte de bordes redondeados[cite: 5, 6].
 * - paddingHorizontal: Dp -> Margen interno horizontal.
 * - paddingVertical: Dp -> Margen interno vertical.
 * - fontSizeTexto: TextUnit -> Tamaño de la letra.
 * - fontWeightTexto: FontWeight -> Grosor de la letra.
 */
@Composable
fun ChipTag(
    texto: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.START,
    conChip: Boolean = true,
    espaciado: Dp = 6.dp,
    colorFondo: Color = MaterialTheme.colorScheme.surfaceVariant,
    colorTexto: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tintIcono: Color = colorTexto,
    forma: Shape = RoundedCornerShape(16.dp),
    paddingHorizontal: Dp = 12.dp,
    paddingVertical: Dp = 6.dp,
    fontSizeTexto: TextUnit = 12.sp,
    fontWeightTexto: FontWeight = FontWeight.Medium,
) {
    ChipSuperficie(
        modifier = modifier,
        enabled = conChip,
        colorFondo = colorFondo,
        forma = forma,
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(espaciado),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Caso 1: Ícono al Inicio
            if (icono != null && iconPosition == IconPosition.START) {
                IconoEstado(
                    icono = icono,
                    tint = tintIcono,
                )
            }

            // Texto central obligado
            TextoEtiqueta(
                texto = texto,
                color = colorTexto,
                fontSize = fontSizeTexto,
                fontWeight = fontWeightTexto,
            )

            // Caso 2: Ícono al Final
            if (icono != null && iconPosition == IconPosition.END) {
                IconoEstado(
                    icono = icono,
                    tint = tintIcono,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Súper Molécula: ChipTag Versátil")
@Composable
fun ChipTagPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 1. Caso: Ícono + Texto + Con Chip ("Disponible")[cite: 4]
        ChipTag(
            texto = "Disponible",
            icono = Icons.Outlined.Circle,
            colorFondo = Color(0xFFE8F5E9),
            colorTexto = Color(0xFF2E7D32),
        )

        // 2. Caso: Solo Texto + Con Chip ("Cariñoso")[cite: 4]
        ChipTag(
            texto = "Cariñoso",
            colorFondo = Color(0xFFE3F2FD),
            colorTexto = Color(0xFF1976D2),
        )

        // 3. Caso: Ícono + Texto + SIN Chip / Plano ("Vacunado")[cite: 4]
        ChipTag(
            texto = "Vacunado",
            icono = Icons.Outlined.CheckCircle,
            conChip = false,
            colorTexto = Color(0xFF1976D2),
        )

        // 4. Caso: Texto + Ícono al final + SIN Chip ("Ubicación")[cite: 4]
        ChipTag(
            texto = "Querétaro, Qro.",
            icono = Icons.Outlined.LocationOn,
            iconPosition = IconPosition.END,
            conChip = false,
        )
    }
}
