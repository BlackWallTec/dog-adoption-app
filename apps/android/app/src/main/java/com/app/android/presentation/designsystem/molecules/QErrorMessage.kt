package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Ícono QIcons.Error + texto, ambos en color error (Caption).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ErrorMessage.
 */
@Composable
fun QErrorMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        // liveRegion: el lector de pantalla anuncia el error en cuanto aparece
        modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QIcon(icon = QIcons.Error, contentDescription = null, tint = QTheme.colors.error, size = QSize.iconSmall)
        QText(text = text, style = QTextStyle.Caption, color = QTheme.colors.error, modifier = Modifier.weight(1f, fill = false))
    }
}

@Preview(showBackground = true, name = "QErrorMessage")
@Composable
private fun QErrorMessagePreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QErrorMessage(text = "No se pudo enviar tu solicitud.")
            QErrorMessage(
                text = "Este es un mensaje de error largo que debe partirse en varias líneas sin salirse de la pantalla del teléfono.",
            )
        }
    }
}