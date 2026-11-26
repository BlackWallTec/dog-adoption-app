// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Encabezado de éxito (Solicitud enviada, Inscripción confirmada, Donativo enviado): círculo positive
 * con QIcons.CheckCircle en onPositive + título (Title, centrado) + subtítulo (Body, textSecondary,
 * centrado).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ConfirmationHeader, IlustracionConfirmacion.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QSuccessHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QSuccessHeader", modifier)
}
