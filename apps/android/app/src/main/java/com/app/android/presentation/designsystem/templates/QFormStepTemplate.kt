// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Pasos del formulario de solicitud (Requisitos → Confirmar): QScreenScaffold + QTopBar con flecha +
 * QStepIndicator(currentStep, totalSteps) al inicio del contenido + stickyAction
 * QButton(primaryActionText, fillMaxWidth) + SIN nav bar.
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QScreenScaffold, QTopBar, QStepIndicator, QButton.
 * TODO(P4): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QFormStepTemplate(
    title: String,
    subtitle: String?,
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    primaryActionText: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    primaryActionEnabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    QPlaceholder("QFormStepTemplate", modifier, content)
}
