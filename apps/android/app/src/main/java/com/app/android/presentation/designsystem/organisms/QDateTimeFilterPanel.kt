// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Bloque 'Filtrar Programa': QSectionHeader(title, description) + 'Día:' con 2 QLabeledSelectField
 * (Desde/Hasta) + 'Hora:' con 2 QLabeledSelectField + fila con QButton('Limpiar') y QButton('Aplicar
 * filtros') del mismo ancho + QErrorMessage si errorText.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QSectionHeader, QText, QLabeledSelectField, QButton, QErrorMessage.
 * Reemplaza a: FiltrosProgramasPanel, FilterActionBar.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QDateTimeFilterPanel", modifier)
}
