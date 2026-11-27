package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Etiqueta descriptiva (no es botón ni estado): fondo tag (#FFFBF2), borde 1.dp tagBorder (#2E2A22),
 * texto textPrimary, forma pill.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: ChipTag, EtiquetaTipo.
 */
@Composable
fun QTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .heightIn(min = QSize.chipHeight)
                .background(color = QTheme.colors.tag, shape = QShapes.pill)
                .border(width = QSize.border, color = QTheme.colors.tagBorder, shape = QShapes.pill)
                .padding(horizontal = QSpacing.m),
        contentAlignment = Alignment.Center,
    ) {
        QText(text = text, style = QTextStyle.Label, maxLines = 1)
    }
}

@Preview(showBackground = true, name = "QTag")
@Composable
private fun QTagPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QTag(text = "Amigable con niños")
            QTag(text = "Voluntariado")
            QTag(text = "Paseo")
        }
    }
}