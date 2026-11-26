// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Chip de filtro presionable. Seleccionado = fondo action + check + texto onActionStrong. No
 * seleccionado = fondo surface + borde border + texto textPrimary.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: FilterButton.
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    QPlaceholder("QFilterChip", modifier)
}
