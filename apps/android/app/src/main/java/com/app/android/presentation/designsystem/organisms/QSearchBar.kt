// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Buscador pill fondo surface: QIcon(QIcons.Search, onAction) + campo + QIconButton(QIcons.Filter,
 * onAction).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QIconButton, QText.
 * Reemplaza a: SearchBar.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar un compañero",
) {
    QPlaceholder("QSearchBar", modifier)
}
