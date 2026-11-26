// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Tarjeta blanca con una QCopyRow por fila y QDivider entre filas (Banco, Titular, CLABE, Cuenta).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QCopyRow, QDivider.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QBankDataCard(
    rows: List<Pair<String, String>>,
    onCopy: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QBankDataCard", modifier)
}
