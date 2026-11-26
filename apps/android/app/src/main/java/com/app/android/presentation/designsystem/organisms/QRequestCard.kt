// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Tarjeta de solicitud: QPhoto 56.dp + nombre (Title) + 'folio · fecha' (Body, textSecondary) +
 * QStatusChip + QTextLink('Ver detalles', QIcons.ExpandMore/ExpandLess). Si expanded muestra
 * details(). Botón 'Editar' (QButton Small) si onEdit != null. NO lleva indicador de pasos.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QPhoto, QText, QStatusChip, QTextLink, QButton.
 * Reemplaza a: TarjetaSolicitud, TarjetaSolicitudExpandible, DetalleSolicitud.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QRequestCard(
    photo: Any?,
    name: String,
    folio: String,
    date: String,
    statusText: String,
    status: QStatus,
    expanded: Boolean,
    onToggleDetails: () -> Unit,
    modifier: Modifier = Modifier,
    statusIcon: ImageVector? = null,
    onEdit: (() -> Unit)? = null,
    details: @Composable ColumnScope.() -> Unit = {},
) {
    QPlaceholder("QRequestCard", modifier, details)
}
