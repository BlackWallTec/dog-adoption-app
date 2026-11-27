// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.molecules.QInfoTile
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Fila de QInfoTile del mismo ancho (weight 1f) con separación QSpacing.s. items = pares (etiqueta,
 * valor). Lista vacía: no dibuja nada.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QInfoTile.
 * Reemplaza a: TarjetaInfo (datos).
 */
@Composable
fun QInfoTileRow(
    items: List<Pair<String, String?>>,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        items.forEach { (label, value) ->
            QInfoTile(
                label = label,
                value = value,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QInfoTileRow - estados")
@Composable
private fun QInfoTileRowPreview() {
    QarinoTheme {
        QInfoTileRow(
            items =
                listOf(
                    "Edad" to "5 años",
                    "Sexo" to "Macho",
                    "Tamaño" to "Extra grande",
                    "Raza" to "Mestizo",
                ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QInfoTileRow - con valores nulos")
@Composable
private fun QInfoTileRowNullPreview() {
    QarinoTheme {
        QInfoTileRow(
            items = listOf("Edad" to null, "Sexo" to "Hembra"),
        )
    }
}
