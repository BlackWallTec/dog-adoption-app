// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.designsystem.theme.QTextStyle

/**
 * Ícono + texto en fila (pin + 'Refugio Principal', calendario + fecha).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: IconLabel, DatoPrograma.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QIconLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Unspecified,
    style: QTextStyle = QTextStyle.Body,
) {
    QPlaceholder("QIconLabel", modifier)
}
