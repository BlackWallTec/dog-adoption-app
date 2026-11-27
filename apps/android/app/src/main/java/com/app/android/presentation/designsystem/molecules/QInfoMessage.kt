package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Mensaje informativo dentro de QCard: ícono (default QIcons.Info en onAction) + título opcional +
 * texto. important=true usa containerColor pending.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QCard, QIcon, QText.
 * Reemplaza a: InfoCard (las 2), ImportantInfoCard, InfoMessageCard.
 */
@Composable
fun QInfoMessage(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    important: Boolean = false,
) {
    QCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = if (important) QTheme.colors.pending else Color.Unspecified,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            QIcon(icon = icon ?: QIcons.Info, contentDescription = null, tint = QTheme.colors.onAction)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
            ) {
                if (title != null) {
                    QText(text = title, style = QTextStyle.BodyStrong)
                }
                QText(text = text)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QInfoMessage - variantes")
@Composable
private fun QInfoMessagePreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QInfoMessage(text = "Aún no tienes voluntariados agendados.")
            QInfoMessage(title = "Antes de empezar", text = "Ten a la mano tu identificación oficial y un comprobante de domicilio.")
            QInfoMessage(
                title = "Importante",
                text = "Tu solicitud se revisa en un máximo de 3 días hábiles.",
                important = true,
            )
            QInfoMessage(text = "Con otro ícono", icon = QIcons.Help)
        }
    }
}