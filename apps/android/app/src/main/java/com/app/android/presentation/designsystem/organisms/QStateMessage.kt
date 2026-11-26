// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Mensaje de vacío/error a pantalla: ícono opcional (48.dp, textSecondary) + mensaje (Body, centrado)
 * + QButton(actionText) si onAction != null.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QText, QButton.
 * Reemplaza a: EstadoError, mensajes 'No hay...'.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    QPlaceholder("QStateMessage", modifier)
}
