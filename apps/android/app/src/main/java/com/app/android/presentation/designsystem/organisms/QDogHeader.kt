// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Encabezado del detalle: nombre (Title) + QStatusChip a la derecha; debajo
 * QIconLabel(QIcons.Location, location, textSecondary) + QButton('Compartir', size Small, icon
 * QIcons.Share) a la derecha, abajo del chip.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QText, QStatusChip, QIconLabel, QButton.
 * Reemplaza a: TarjetaInfo (encabezado).
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QDogHeader", modifier)
}
