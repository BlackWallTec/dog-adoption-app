// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Fila de dato copiable: etiqueta (Label, textSecondary), valor (BodyStrong) y
 * QIconButton(QIcons.Copy) en onAction a la derecha.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText, QIconButton.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QCopyRow", modifier)
}
