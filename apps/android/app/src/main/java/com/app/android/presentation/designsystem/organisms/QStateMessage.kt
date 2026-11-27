// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val ICON_SIZE = 48.dp

/**
 * Mensaje de pantalla vacía o de error: ícono opcional (textSecondary) + mensaje centrado + botón de acción
 * si hay actionText y onAction. Centrado con padding xxl; el tamaño lo decide quien lo usa (modifier).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QText, QButton.
 * Reemplaza a: EstadoError, mensajes "No hay…".
 */
@Composable
fun QStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(QSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            // El ícono es decorativo: el mensaje ya lo explica.
            QIcon(
                icon = icon,
                contentDescription = null,
                tint = QTheme.colors.textSecondary,
                size = ICON_SIZE,
            )
            Spacer(modifier = Modifier.height(QSpacing.m))
        }
        QText(
            text = message,
            style = QTextStyle.Body,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(QSpacing.m))
            QButton(
                text = actionText,
                onClick = onAction,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QStateMessage - estados")
@Composable
private fun QStateMessagePreview() {
    QarinoTheme {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xl)) {
            QStateMessage(
                message = "Aún no tienes voluntariados agendados.",
                icon = QIcons.Info,
            )
            QStateMessage(
                message = "No pudimos cargar los perros. Revisa tu conexión.",
                icon = QIcons.Error,
                actionText = "Reintentar",
                onAction = {},
            )
            QStateMessage(message = "No hay solicitudes por ahora.")
        }
    }
}
