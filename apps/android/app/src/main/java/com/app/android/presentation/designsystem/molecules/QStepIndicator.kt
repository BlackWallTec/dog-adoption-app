// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Indicador de pasos del formulario de solicitud: totalSteps barritas, las completadas en onAction,
 * las demás en border + texto 'Paso X de N' (Caption).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText.
 * Reemplaza a: IndicadorPasos.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QStepIndicator", modifier)
}
