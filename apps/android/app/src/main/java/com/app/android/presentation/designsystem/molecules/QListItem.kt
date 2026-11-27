package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Fila de lista presionable (Ajustes/Cuenta): ícono opcional, título, subtítulo opcional y
 * QIcons.ChevronRight.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ListAccess.
 */
@Composable
fun QListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = QSize.buttonHeight)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = QSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            QIcon(icon = icon, contentDescription = null, tint = QTheme.colors.onAction)
        }
        Column(modifier = Modifier.weight(1f)) {
            QText(text = title, style = QTextStyle.BodyStrong)
            if (subtitle != null) {
                QText(text = subtitle, style = QTextStyle.Caption)
            }
        }
        QIcon(icon = QIcons.ChevronRight, contentDescription = null, tint = QTheme.colors.textSecondary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QListItem - con y sin ícono / subtítulo")
@Composable
private fun QListItemPreview() {
    QarinoTheme {
        QCard(modifier = Modifier.fillMaxWidth().padding(QSpacing.l)) {
            QListItem(title = "Mi cuenta", subtitle = "Nombre, correo y contraseña", icon = QIcons.Settings, onClick = {})
            QDivider()
            QListItem(title = "Mis donativos", icon = QIcons.Donate, onClick = {})
            QDivider()
            QListItem(title = "Aviso de privacidad", subtitle = "Cómo cuidamos tus datos", onClick = {})
            QDivider()
            QListItem(title = "Cerrar sesión", onClick = {})
        }
    }
}