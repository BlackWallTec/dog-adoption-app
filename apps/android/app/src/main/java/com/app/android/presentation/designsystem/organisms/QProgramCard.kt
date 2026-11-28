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
import com.app.android.presentation.designsystem.atoms.QButtonVariant
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QTag
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QCapacity
import com.app.android.presentation.designsystem.molecules.QIconLabel
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Tarjeta de programa de voluntariado: QTag(tipo) + chevron arriba; título (Title); fecha, hora y lugar con
 * QIconLabel; cupo con QCapacity. Toda la tarjeta llama onClick. Botones según estado: inscrito → cancelar
 * (si onCancel != null); no inscrito → inscribirme (si onEnroll != null, deshabilitado si está lleno).
 *
 * El texto y el estado de los botones se pueden cambiar (enrollText, enrollEnabled, cancelText, cancelEnabled)
 * para mostrar "Sin lugares", "Ya comenzó", "Inscribiendo…", "Cancelando…", etc.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QTag, QIconButton, QText, QIconLabel, QCapacity, QButton.
 * Reemplaza a: TarjetaVoluntariado, TarjetaFechaLugar.
 */
@Composable
fun QProgramCard(
    type: String,
    title: String,
    date: String,
    time: String,
    location: String,
    capacityText: String,
    capacityProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFull: Boolean = false,
    enrolled: Boolean = false,
    onEnroll: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    enrollText: String = "Inscribirme",
    enrollEnabled: Boolean = true,
    cancelText: String = "Cancelar inscripción",
    cancelEnabled: Boolean = true,
) {
    val colors = QTheme.colors
    QCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QTag(text = type)
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    QIconButton(
                        icon = QIcons.ChevronRight,
                        contentDescription = "Ver detalles del programa",
                        onClick = onClick,
                    )
                }
            }
            QText(
                text = title,
                style = QTextStyle.Title,
                modifier = Modifier.semantics { heading() },
            )
            Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xs)) {
                QIconLabel(
                    icon = QIcons.Calendar,
                    text = date,
                    iconTint = colors.textSecondary,
                )
                QIconLabel(
                    icon = QIcons.Clock,
                    text = time,
                    iconTint = colors.textSecondary,
                )
                QIconLabel(
                    icon = QIcons.Location,
                    text = location,
                    iconTint = colors.textSecondary,
                )
            }
            QCapacity(
                text = capacityText,
                progress = capacityProgress,
                isFull = isFull,
            )
            if (enrolled && onCancel != null) {
                QButton(
                    text = cancelText,
                    onClick = onCancel,
                    variant = QButtonVariant.Destructive,
                    enabled = cancelEnabled,
                    fillMaxWidth = true,
                )
            } else if (!enrolled && onEnroll != null) {
                QButton(
                    text = enrollText,
                    onClick = onEnroll,
                    enabled = enrollEnabled && !isFull,
                    fillMaxWidth = true,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QProgramCard - estados")
@Composable
private fun QProgramCardPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QProgramCard(
                type = "Voluntariado",
                title = "Paseos y socialización de fin de semana",
                date = "Dom. 15 nov. 2026",
                time = "09:00 – 14:00 h",
                location = "Refugio Qariño · Querétaro",
                capacityText = "0/25 lugares ocupados · 25 disponibles",
                capacityProgress = 0f,
                onClick = {},
                onEnroll = {},
            )
            QProgramCard(
                type = "Paseo",
                title = "Limpieza de instalaciones",
                date = "Sáb. 21 nov. 2026",
                time = "10:00 – 12:00 h",
                location = "Refugio Principal",
                capacityText = "25/25 lugares ocupados",
                capacityProgress = 1f,
                isFull = true,
                onClick = {},
                onEnroll = {},
                enrollText = "Sin lugares",
            )
            QProgramCard(
                type = "Voluntariado",
                title = "Programa que ya comenzó",
                date = "Dom. 15 nov. 2026",
                time = "09:00 – 11:00 h",
                location = "Refugio Principal",
                capacityText = "5/25 lugares ocupados",
                capacityProgress = 0.2f,
                onClick = {},
                onEnroll = {},
                enrollText = "Ya comenzó",
                enrollEnabled = false,
            )
            QProgramCard(
                type = "Voluntariado",
                title = "Ya estás inscrito",
                date = "Dom. 22 nov. 2026",
                time = "09:00 – 11:00 h",
                location = "Refugio Principal",
                capacityText = "12/25 lugares ocupados",
                capacityProgress = 0.48f,
                enrolled = true,
                onClick = {},
                onCancel = {},
            )
            QProgramCard(
                type = "Voluntariado",
                title = "Cancelando inscripción",
                date = "Dom. 22 nov. 2026",
                time = "09:00 – 11:00 h",
                location = "Refugio Principal",
                capacityText = "12/25 lugares ocupados",
                capacityProgress = 0.48f,
                enrolled = true,
                onClick = {},
                onCancel = {},
                cancelText = "Cancelando…",
                cancelEnabled = false,
            )
        }
    }
}