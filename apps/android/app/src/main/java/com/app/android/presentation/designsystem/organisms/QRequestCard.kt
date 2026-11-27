// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QButtonSize
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QPhoto
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QStatusChip
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.atoms.QTextLink
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val PHOTO_SIZE = 56.dp

/**
 * Tarjeta de solicitud: foto 56.dp + nombre (Title) + folio · fecha (textSecondary) + chip de estado y
 * link Ver detalles / Ocultar detalles. Si expanded muestra details(). Botón Editar si onEdit != null.
 * NO lleva indicador de pasos.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QPhoto, QText, QStatusChip, QTextLink, QButton.
 * Reemplaza a: TarjetaSolicitud, TarjetaSolicitudExpandible, DetalleSolicitud.
 */
@Composable
fun QRequestCard(
    photo: Any?,
    name: String,
    folio: String,
    date: String,
    statusText: String,
    status: QStatus,
    expanded: Boolean,
    onToggleDetails: () -> Unit,
    modifier: Modifier = Modifier,
    statusIcon: ImageVector? = null,
    onEdit: (() -> Unit)? = null,
    details: @Composable ColumnScope.() -> Unit = {},
) {
    QCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QPhoto(
                    model = photo,
                    contentDescription = null,
                    modifier = Modifier.size(PHOTO_SIZE),
                )
                Spacer(modifier = Modifier.width(QSpacing.m))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
                ) {
                    QText(
                        text = name,
                        style = QTextStyle.Title,
                        maxLines = 1,
                    )
                    QText(
                        text = "$folio · $date",
                        style = QTextStyle.Body,
                        color = QTheme.colors.textSecondary,
                        maxLines = 1,
                    )
                    QStatusChip(
                        text = statusText,
                        status = status,
                        icon = statusIcon,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QTextLink(
                    text = if (expanded) "Ocultar detalles" else "Ver detalles",
                    onClick = onToggleDetails,
                    trailingIcon = if (expanded) QIcons.ExpandLess else QIcons.ExpandMore,
                )
                if (onEdit != null) {
                    QButton(
                        text = "Editar",
                        onClick = onEdit,
                        size = QButtonSize.Small,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    details()
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QRequestCard - estados")
@Composable
private fun QRequestCardPreview() {
    QarinoTheme {
        val expanded = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QRequestCard(
                photo = null,
                name = "Lucia",
                folio = "QA-9AAC227E",
                date = "25 nov 2026",
                statusText = "En revisión",
                status = QStatus.Pending,
                expanded = expanded.value,
                onToggleDetails = { expanded.value = !expanded.value },
                onEdit = {},
            ) {
                QText(text = "Vivienda: casa con patio", style = QTextStyle.Body)
                QText(text = "Vacunas al día", style = QTextStyle.Body)
            }
            QRequestCard(
                photo = null,
                name = "Abraham",
                folio = "QA-0B102963",
                date = "19 nov 2026",
                statusText = "Aprobada",
                status = QStatus.Positive,
                expanded = false,
                onToggleDetails = {},
            )
        }
    }
}
