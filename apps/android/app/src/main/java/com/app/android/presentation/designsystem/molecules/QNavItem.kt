// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Elemento de la nav bar: ícono 24.dp + etiqueta (Caption). Seleccionado: pastilla action (≈56x30.dp)
 * detrás del ícono, ícono y texto en onActionStrong, texto en negritas. No seleccionado:
 * textSecondary.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ButtonNavigation.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QNavItem", modifier)
}
