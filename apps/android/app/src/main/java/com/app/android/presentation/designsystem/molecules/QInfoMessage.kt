// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Mensaje informativo dentro de QCard: ícono (default QIcons.Info en onAction) + título opcional +
 * texto. important=true usa containerColor pending.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QCard, QIcon, QText.
 * Reemplaza a: InfoCard (las 2), ImportantInfoCard, InfoMessageCard.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QInfoMessage(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    important: Boolean = false,
) {
    QPlaceholder("QInfoMessage", modifier)
}
