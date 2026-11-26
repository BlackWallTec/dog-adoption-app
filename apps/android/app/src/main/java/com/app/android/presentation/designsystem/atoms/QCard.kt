// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Contenedor tarjeta: fondo surface (o containerColor), radio QRadius.medium, padding interno
 * QSpacing.l, sin sombra ni borde. Si onClick != null es presionable.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: Card/Surface sueltos.
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    QPlaceholder("QCard", modifier, content)
}
