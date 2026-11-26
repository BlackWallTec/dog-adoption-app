// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * QSectionHeader + contenido con separación QSpacing.s.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QSectionHeader.
 * Reemplaza a: SectionBlock, CareParagraphItem.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    QPlaceholder("QSection", modifier, content)
}
