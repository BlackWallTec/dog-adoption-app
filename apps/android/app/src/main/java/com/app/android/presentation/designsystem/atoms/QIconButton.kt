package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val MAX_BADGE_COUNT = 99

/**
 * Ícono que se puede presionar (flecha, campana, copiar). badgeCount > 0 muestra un globito con el
 * número. Área táctil de 48dp (la de IconButton).
 *
 * Dueño: P1 (Átomos).
 * Usa (obligatorio): QIcon.
 * Reemplaza a: CounterIconButton, IconWithBadge.
 */
@Composable
fun QIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    badgeCount: Int = 0,
    enabled: Boolean = true,
) {
    val resolvedTint =
        when {
            !enabled -> QTheme.colors.disabledContent
            tint != Color.Unspecified -> tint
            else -> QTheme.colors.textPrimary
        }
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = QTheme.colors.onAction) {
                        QText(
                            text = if (badgeCount > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else badgeCount.toString(),
                            style = QTextStyle.Caption,
                            color = QTheme.colors.surface,
                        )
                    }
                },
            ) {
                QIcon(icon = icon, contentDescription = contentDescription, tint = resolvedTint)
            }
        } else {
            QIcon(icon = icon, contentDescription = contentDescription, tint = resolvedTint)
        }
    }
}

@Preview(showBackground = true, name = "QIconButton - estados")
@Composable
private fun QIconButtonPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QIconButton(icon = QIcons.Back, contentDescription = "Regresar", onClick = {})
            QIconButton(icon = QIcons.Notifications, contentDescription = "Notificaciones", onClick = {})
            QIconButton(icon = QIcons.Notifications, contentDescription = "Notificaciones", onClick = {}, badgeCount = 3)
            QIconButton(icon = QIcons.Notifications, contentDescription = "Notificaciones", onClick = {}, badgeCount = 120)
            QIconButton(icon = QIcons.Copy, contentDescription = "Copiar", onClick = {}, tint = QTheme.colors.onAction)
            QIconButton(icon = QIcons.Copy, contentDescription = "Copiar", onClick = {}, enabled = false)
        }
    }
}