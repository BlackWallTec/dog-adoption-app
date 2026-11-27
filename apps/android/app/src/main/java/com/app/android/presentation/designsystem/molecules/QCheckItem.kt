package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Ícono QIcons.CheckCircle en onPositive + texto (Vacunado, Esterilizado). Si checked=false el
 * ícono va en textSecondary.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 */
@Composable
fun QCheckItem(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean = true,
) {
    Row(
        modifier =
            modifier.semantics(mergeDescendants = true) {
                // El color del ícono no lo "ve" un lector de pantalla, así que el estado se dice con palabras
                stateDescription = if (checked) "Sí" else "No"
            },
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QIcon(
            icon = QIcons.CheckCircle,
            contentDescription = null,
            tint = if (checked) QTheme.colors.onPositive else QTheme.colors.textSecondary,
            size = QSize.iconSmall,
        )
        QText(text = text)
    }
}

@Preview(showBackground = true, name = "QCheckItem - marcado y sin marcar")
@Composable
private fun QCheckItemPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QCheckItem(text = "Vacunado")
            QCheckItem(text = "Esterilizado")
            QCheckItem(text = "Desparasitado", checked = false)
        }
    }
}