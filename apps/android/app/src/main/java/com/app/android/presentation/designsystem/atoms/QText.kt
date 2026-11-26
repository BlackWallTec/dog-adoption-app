// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.designsystem.theme.QTextStyle

/**
 * Texto con los estilos del tema. Si color es Unspecified usa el color por defecto del estilo
 * (textPrimary; Caption usa textSecondary).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: TextoTitulo, TextoCuerpo, TextoEtiqueta, SupportingText, HelperText.
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QText(
    text: String,
    modifier: Modifier = Modifier,
    style: QTextStyle = QTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    QPlaceholder("QText", modifier)
}
