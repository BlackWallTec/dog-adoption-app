// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterOptions
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterSelection

/**
 * Bottom sheet de filtros del catálogo (misma lógica que el FilterSheet actual) con QSectionHeader,
 * QFilterChipGroup, QLabeledSelectField y botones QButton 'Limpiar' / 'Aplicar filtros'. Fondo
 * surface.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QSectionHeader, QFilterChipGroup, QLabeledSelectField, QButton.
 * Reemplaza a: FilterSheet, FilterSheetContent.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QCatalogFilterSheet(
    applied: FilterSelection,
    options: FilterOptions,
    onApply: (FilterSelection) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isVisible) {
        QPlaceholder("QCatalogFilterSheet", modifier)
    }
}
