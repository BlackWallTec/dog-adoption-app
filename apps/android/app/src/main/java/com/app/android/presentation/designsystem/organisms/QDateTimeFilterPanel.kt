// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QLabeledSelectField
import com.app.android.presentation.designsystem.molecules.QSectionHeader
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Bloque "Filtrar Programa": encabezado de sección, selectores de día (Desde/Hasta) y hora (Desde/Hasta)
 * en filas de 2 columnas iguales, botones Limpiar y Aplicar filtros del mismo ancho y mensaje de error
 * opcional. Los selectores solo avisan con su onClick; quien lo usa abre los pickers.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QSectionHeader, QText, QLabeledSelectField, QButton, QErrorMessage.
 * Reemplaza a: FiltrosProgramasPanel, FilterActionBar.
 */
@Composable
fun QDateTimeFilterPanel(
    fromDate: String?,
    toDate: String?,
    fromTime: String?,
    toTime: String?,
    onFromDateClick: () -> Unit,
    onToDateClick: () -> Unit,
    onFromTimeClick: () -> Unit,
    onToTimeClick: () -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Filtrar Programa",
    description: String = "Selecciona las fechas y el horario de tu preferencia.",
    errorText: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QSpacing.m),
    ) {
        QSectionHeader(
            title = title,
            subtitle = description,
        )
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            QText(text = "Día:", style = QTextStyle.BodyStrong)
            Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                QLabeledSelectField(
                    label = "Desde",
                    value = fromDate,
                    onClick = onFromDateClick,
                    modifier = Modifier.weight(1f),
                )
                QLabeledSelectField(
                    label = "Hasta",
                    value = toDate,
                    onClick = onToDateClick,
                    modifier = Modifier.weight(1f),
                )
            }
            QText(text = "Hora:", style = QTextStyle.BodyStrong)
            Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                QLabeledSelectField(
                    label = "Desde",
                    value = fromTime,
                    onClick = onFromTimeClick,
                    modifier = Modifier.weight(1f),
                )
                QLabeledSelectField(
                    label = "Hasta",
                    value = toTime,
                    onClick = onToTimeClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            QButton(
                text = "Limpiar",
                onClick = onClear,
                fillMaxWidth = true,
                modifier = Modifier.weight(1f),
            )
            QButton(
                text = "Aplicar filtros",
                onClick = onApply,
                fillMaxWidth = true,
                modifier = Modifier.weight(1f),
            )
        }
        if (errorText != null) {
            QErrorMessage(text = errorText)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QDateTimeFilterPanel - estados")
@Composable
private fun QDateTimeFilterPanelPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xxl),
        ) {
            QDateTimeFilterPanel(
                fromDate = null,
                toDate = null,
                fromTime = null,
                toTime = null,
                onFromDateClick = {},
                onToDateClick = {},
                onFromTimeClick = {},
                onToTimeClick = {},
                onClear = {},
                onApply = {},
            )
            QDateTimeFilterPanel(
                fromDate = "Dom. 15 nov. 2026",
                toDate = "Dom. 22 nov. 2026",
                fromTime = "09:00",
                toTime = "14:00",
                onFromDateClick = {},
                onToDateClick = {},
                onFromTimeClick = {},
                onToTimeClick = {},
                onClear = {},
                onApply = {},
                errorText = "La fecha final debe ser posterior a la inicial.",
            )
        }
    }
}
