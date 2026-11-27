// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.molecules.QCopyRow
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Tarjeta blanca con una QCopyRow por dato y QDivider entre filas (sin divider después de la última).
 * rows = pares (etiqueta, valor); onCopy recibe ambos. Lista vacía: no dibuja nada.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QCopyRow, QDivider.
 */
@Composable
fun QBankDataCard(
    rows: List<Pair<String, String>>,
    onCopy: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return
    QCard(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (label, value) ->
            QCopyRow(
                label = label,
                value = value,
                onCopy = { onCopy(label, value) },
            )
            if (index < rows.lastIndex) {
                QDivider()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QBankDataCard - datos bancarios")
@Composable
private fun QBankDataCardPreview() {
    QarinoTheme {
        QBankDataCard(
            rows =
                listOf(
                    "Banco" to "BANCO",
                    "Titular" to "TITULAR",
                    "CLABE" to "000000000000000000",
                    "Cuenta" to "0000000000",
                ),
            onCopy = { _, _ -> },
        )
    }
}
