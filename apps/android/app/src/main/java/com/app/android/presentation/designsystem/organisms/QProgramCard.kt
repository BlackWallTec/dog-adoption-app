// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Tarjeta de programa de voluntariado: QTag(type) + QIconButton(QIcons.ChevronRight) arriba; título
 * (Title); QIconLabel calendario/fecha, reloj/hora, pin/lugar; QCapacity. Botones: si enrolled ->
 * QButton('Cancelar inscripción', Destructive) si onCancel != null; si no -> QButton('Inscribirme',
 * enabled = !isFull) si onEnroll != null. Toda la tarjeta llama onClick.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QTag, QIconButton, QText, QIconLabel, QCapacity, QButton.
 * Reemplaza a: TarjetaVoluntariado, TarjetaFechaLugar.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
) {
    QPlaceholder("QProgramCard", modifier)
}
