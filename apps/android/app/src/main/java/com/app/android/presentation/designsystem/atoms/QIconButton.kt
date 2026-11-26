// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Ícono que se puede presionar (flecha, campana, copiar). badgeCount > 0 muestra un globito con el
 * número.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: CounterIconButton, IconWithBadge.
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    badgeCount: Int = 0,
    enabled: Boolean = true,
) {
    QPlaceholder("QIconButton", modifier)
}
