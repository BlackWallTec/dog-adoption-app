package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Ícono + texto en fila (pin + 'Refugio Principal', calendario + fecha).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: IconLabel, DatoPrograma.
 */
@Composable
fun QIconLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.Unspecified,
    style: QTextStyle = QTextStyle.Body,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // El ícono es decorativo: el texto ya dice lo mismo para los lectores de pantalla
        QIcon(icon = icon, contentDescription = null, tint = iconTint, size = QSize.iconSmall)
        QText(text = text, style = style, modifier = Modifier.weight(1f, fill = false))
    }
}

@Preview(showBackground = true, name = "QIconLabel - variantes")
@Composable
private fun QIconLabelPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QIconLabel(icon = QIcons.Location, text = "Refugio Principal")
            QIconLabel(icon = QIcons.Calendar, text = "Dom. 15 nov. 2026", iconTint = QTheme.colors.textSecondary)
            QIconLabel(icon = QIcons.Clock, text = "09:00 – 14:00 h", style = QTextStyle.BodyStrong)
            QIconLabel(icon = QIcons.Location, text = "Refugio Qariño", style = QTextStyle.Caption, iconTint = QTheme.colors.onAction)
            QIconLabel(
                icon = QIcons.Location,
                text = "Un texto muy largo que no cabe en una sola línea y tiene que seguir dentro de la pantalla sin empujar nada",
            )
        }
    }
}