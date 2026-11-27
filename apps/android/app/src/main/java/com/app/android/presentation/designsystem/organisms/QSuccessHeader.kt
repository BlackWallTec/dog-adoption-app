// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val BADGE_SIZE = 72.dp
private val BADGE_ICON_SIZE = 40.dp

/**
 * Encabezado de éxito (Solicitud enviada, Inscripción confirmada, Donativo enviado): círculo positive con
 * CheckCircle en onPositive, título (Title) y subtítulo (Body, textSecondary), ambos centrados.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ConfirmationHeader, IlustracionConfirmacion.
 */
@Composable
fun QSuccessHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colors = QTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QSpacing.m),
    ) {
        Box(
            modifier =
                Modifier
                    .size(BADGE_SIZE)
                    .clip(QShapes.pill)
                    .background(colors.positive),
            contentAlignment = Alignment.Center,
        ) {
            // Decorativo: el título ya anuncia el éxito.
            QIcon(
                icon = QIcons.CheckCircle,
                contentDescription = null,
                tint = colors.onPositive,
                size = BADGE_ICON_SIZE,
            )
        }
        QText(
            text = title,
            style = QTextStyle.Title,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        QText(
            text = subtitle,
            style = QTextStyle.Body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QSuccessHeader - estados")
@Composable
private fun QSuccessHeaderPreview() {
    QarinoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(QSpacing.xxl)) {
            QSuccessHeader(
                title = "Solicitud enviada",
                subtitle = "Revisaremos tu solicitud y te avisaremos por correo.",
            )
        }
    }
}
