// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QButtonSize
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QStatusChip
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QIconLabel
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Encabezado del detalle: nombre (Title) + chip de estado a la derecha; debajo la ubicación (textSecondary)
 * y el botón Compartir a la derecha.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QText, QStatusChip, QIconLabel, QButton.
 * Reemplaza a: TarjetaInfo (encabezado).
 */
@Composable
fun QDogHeader(
    name: String,
    statusText: String,
    status: QStatus,
    location: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QText(
                text = name,
                style = QTextStyle.Title,
                maxLines = 2,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            QStatusChip(text = statusText, status = status)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            // Caption ya usa textSecondary por defecto: la ubicación queda en ese color.
            QIconLabel(
                icon = QIcons.Location,
                text = location,
                iconTint = QTheme.colors.textSecondary,
                style = QTextStyle.Caption,
                modifier = Modifier.weight(1f),
            )
            QButton(
                text = "Compartir",
                onClick = onShare,
                size = QButtonSize.Small,
                icon = QIcons.Share,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QDogHeader - estados")
@Composable
private fun QDogHeaderPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xl),
        ) {
            QDogHeader(
                name = "Señor Salchicha",
                statusText = "Disponible",
                status = QStatus.Positive,
                location = "Refugio Principal",
                onShare = {},
            )
            QDogHeader(
                name = "Bethoveen con un nombre largo que ocupa dos líneas en pantalla",
                statusText = "Sin lugares",
                status = QStatus.Negative,
                location = "Refugio Qariño · Querétaro, Santiago de Querétaro",
                onShare = {},
            )
        }
    }
}
