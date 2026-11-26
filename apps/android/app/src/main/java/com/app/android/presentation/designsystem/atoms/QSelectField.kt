// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Campo que abre un selector (fecha, hora, lista). Mismo estilo visual que el campo de texto pero en
 * forma pill; muestra placeholder si value es null.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: FilterDropdown (la parte visual).
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QSelectField(
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar",
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    QPlaceholder("QSelectField", modifier)
}
