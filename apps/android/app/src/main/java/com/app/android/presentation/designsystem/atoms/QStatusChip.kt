// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

enum class QStatus { Positive, Pending, Negative, Neutral }

/**
 * Chip de ESTADO (no es botón). Positive = positive/onPositive (Disponible, Aprobada). Pending =
 * pending/onPending (En revisión). Negative = negative/onNegative (No aprobada, Sin lugares). Neutral
 * = surfaceMuted/textPrimary. Ícono por defecto: Positive=QIcons.Check, Pending=QIcons.Clock,
 * Negative=QIcons.Close, Neutral=ninguno.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: EtiquetaEstado, ChipSuperficie, EstadoTarjeta (colores).
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QStatusChip(
    text: String,
    status: QStatus,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showIcon: Boolean = true,
) {
    QPlaceholder("QStatusChip", modifier)
}
