// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Tarjeta de resumen (paso Confirmar): título (Title) + QTextLink('Editar') si onEdit != null +
 * contenido.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QText, QTextLink.
 * Reemplaza a: TarjetaResumen.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QSummaryCard(
    title: String,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    QPlaceholder("QSummaryCard", modifier, content)
}
