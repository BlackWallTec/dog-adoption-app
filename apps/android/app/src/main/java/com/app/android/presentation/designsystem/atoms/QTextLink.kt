package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Texto presionable en onAction (ej. 'Ver detalles ⌄'). Sin fondo.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: AppButton(variant = Clear).
 */
@Composable
fun QTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val color = if (enabled) QTheme.colors.onAction else QTheme.colors.disabledContent
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = color, disabledContentColor = color),
    ) {
        QText(text = text, style = QTextStyle.BodyStrong, color = color, maxLines = 1)
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(QSpacing.xs))
            QIcon(icon = trailingIcon, contentDescription = null, tint = color, size = QSize.iconSmall)
        }
    }
}

@Preview(showBackground = true, name = "QTextLink")
@Composable
private fun QTextLinkPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QTextLink(text = "Ver detalles", onClick = {}, trailingIcon = QIcons.ExpandMore)
            QTextLink(text = "Ver mis donativos", onClick = {})
            QTextLink(text = "Deshabilitado", onClick = {}, enabled = false)
        }
    }
}