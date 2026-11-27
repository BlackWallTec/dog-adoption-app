package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QProgressBar
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * QProgressBar + texto de cupo (Caption, textSecondary) debajo.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QProgressBar, QText.
 * Reemplaza a: CupoPrograma.
 */
@Composable
fun QCapacity(
    text: String,
    progress: Float,
    modifier: Modifier = Modifier,
    isFull: Boolean = false,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        QProgressBar(progress = progress, isFull = isFull)
        QText(text = text, style = QTextStyle.Caption, color = QTheme.colors.textSecondary)
    }
}

@Preview(showBackground = true, name = "QCapacity - vacío, medio y lleno")
@Composable
private fun QCapacityPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QCapacity(text = "0/25 lugares ocupados · 25 disponibles", progress = 0f)
            QCapacity(text = "13/25 lugares ocupados · 12 disponibles", progress = 0.52f)
            QCapacity(text = "25/25 lugares ocupados · 0 disponibles", progress = 1f, isFull = true)
        }
    }
}