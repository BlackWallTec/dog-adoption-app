// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * QIconButton(QIcons.Remove) + número + QIconButton(QIcons.Add), respeta min/max.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIconButton, QText.
 * Reemplaza a: SumRestButton.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
) {
    QPlaceholder("QCounter", modifier)
}
