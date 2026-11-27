package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

enum class QStatus { Positive, Pending, Negative, Neutral }

/**
 * Chip de ESTADO (no es botón). Positive = positive/onPositive (Disponible, Aprobada). Pending =
 * pending/onPending (En revisión). Negative = negative/onNegative (No aprobada, Sin lugares). Neutral =
 * surfaceMuted/textPrimary. Ícono por defecto: Positive=QIcons.Check, Pending=QIcons.Clock,
 * Negative=QIcons.Close, Neutral=ninguno.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: EtiquetaEstado, ChipSuperficie, EstadoTarjeta (colores).
 */
@Composable
fun QStatusChip(
    text: String,
    status: QStatus,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showIcon: Boolean = true,
) {
    val colors = QTheme.colors
    val background =
        when (status) {
            QStatus.Positive -> colors.positive
            QStatus.Pending -> colors.pending
            QStatus.Negative -> colors.negative
            QStatus.Neutral -> colors.surfaceMuted
        }
    val content =
        when (status) {
            QStatus.Positive -> colors.onPositive
            QStatus.Pending -> colors.onPending
            QStatus.Negative -> colors.onNegative
            QStatus.Neutral -> colors.textPrimary
        }
    val resolvedIcon =
        icon ?: when (status) {
            QStatus.Positive -> QIcons.Check
            QStatus.Pending -> QIcons.Clock
            QStatus.Negative -> QIcons.Close
            QStatus.Neutral -> null
        }
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.chipHeight)
                .background(color = background, shape = QShapes.pill)
                .padding(horizontal = QSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QSpacing.xs),
    ) {
        if (showIcon && resolvedIcon != null) {
            QIcon(icon = resolvedIcon, contentDescription = null, tint = content, size = QSize.iconSmall)
        }
        QText(text = text, style = QTextStyle.Label, color = content, maxLines = 1)
    }
}

@Preview(showBackground = true, name = "QStatusChip")
@Composable
private fun QStatusChipPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QStatusChip(text = "Disponible", status = QStatus.Positive)
            QStatusChip(text = "Aprobada", status = QStatus.Positive)
            QStatusChip(text = "En revisión", status = QStatus.Pending)
            QStatusChip(text = "No aprobada", status = QStatus.Negative)
            QStatusChip(text = "Sin lugares", status = QStatus.Negative, showIcon = false)
            QStatusChip(text = "Borrador", status = QStatus.Neutral)
        }
    }
}