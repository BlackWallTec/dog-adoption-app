// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Diálogo de confirmación (AlertDialog) fondo surface: título (Title), descripción (Body),
 * QButton(confirmText, Destructive si destructive) y QTextLink(dismissText).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QText, QButton, QTextLink.
 * Reemplaza a: ConfirmActionDialog.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QConfirmDialog(
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Volver",
    destructive: Boolean = false,
) {
    QPlaceholder("QConfirmDialog", modifier)
}
