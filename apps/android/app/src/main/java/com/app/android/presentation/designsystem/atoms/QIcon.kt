package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Ícono. Si tint es Unspecified usa textPrimary.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: IconoEstado.
 */
@Composable
fun QIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = QSize.icon,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = if (tint != Color.Unspecified) tint else QTheme.colors.textPrimary,
    )
}

@Preview(showBackground = true, name = "QIcon - colores y tamaños")
@Composable
private fun QIconPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QIcon(icon = QIcons.Paw, contentDescription = null)
            QIcon(icon = QIcons.Location, contentDescription = null, tint = QTheme.colors.textSecondary)
            QIcon(icon = QIcons.Search, contentDescription = null, tint = QTheme.colors.onAction)
            QIcon(icon = QIcons.CheckCircle, contentDescription = null, tint = QTheme.colors.onPositive)
            QIcon(icon = QIcons.Error, contentDescription = null, tint = QTheme.colors.error)
            QIcon(icon = QIcons.Clock, contentDescription = null, size = QSize.iconSmall)
            QIcon(icon = QIcons.Paw, contentDescription = null, size = QSize.topBarLogo)
        }
    }
}