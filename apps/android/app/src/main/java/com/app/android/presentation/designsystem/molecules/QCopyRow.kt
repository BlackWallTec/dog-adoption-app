package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Fila de dato copiable: etiqueta (Label, textSecondary), valor (BodyStrong) y QIconButton(QIcons.Copy)
 * en onAction a la derecha.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText, QIconButton.
 */
@Composable
fun QCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = QSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            QText(text = label, style = QTextStyle.Label, color = QTheme.colors.textSecondary)
            QText(text = value, style = QTextStyle.BodyStrong)
        }
        QIconButton(
            icon = QIcons.Copy,
            contentDescription = "Copiar $label",
            onClick = onCopy,
            tint = QTheme.colors.onAction,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QCopyRow")
@Composable
private fun QCopyRowPreview() {
    QarinoTheme {
        Column(modifier = Modifier.padding(horizontal = QSpacing.l)) {
            QCopyRow(label = "Banco", value = "BBVA", onCopy = {})
            QDivider()
            QCopyRow(label = "Titular", value = "Refugio Qariño A.C.", onCopy = {})
            QDivider()
            QCopyRow(label = "CLABE", value = "012345678901234567", onCopy = {})
        }
    }
}