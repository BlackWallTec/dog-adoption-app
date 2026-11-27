package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val EMPTY_VALUE = "—"

/**
 * Recuadro de dato: etiqueta arriba (Label, textSecondary) y valor abajo (BodyStrong). Fondo
 * surfaceMuted, radio QRadius.small. Si value es null muestra '—'.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText.
 * Reemplaza a: DatoResumen.
 */
@Composable
fun QInfoTile(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(QShapes.small)
                .background(QTheme.colors.surfaceMuted)
                .padding(horizontal = QSpacing.m, vertical = QSpacing.s)
                .semantics(mergeDescendants = true) {},
    ) {
        QText(text = label, style = QTextStyle.Label, color = QTheme.colors.textSecondary)
        QText(text = value ?: EMPTY_VALUE, style = QTextStyle.BodyStrong)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QInfoTile - con y sin valor")
@Composable
private fun QInfoTilePreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QInfoTile(label = "Edad", value = "5 años")
            QInfoTile(label = "Sexo", value = "Macho")
            QInfoTile(label = "Tamaño", value = "Chico")
            QInfoTile(label = "Raza", value = null)
        }
    }
}