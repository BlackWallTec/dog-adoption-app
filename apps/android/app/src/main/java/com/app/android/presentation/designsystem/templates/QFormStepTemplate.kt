package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QStepIndicator
import com.app.android.presentation.designsystem.organisms.QTopBar
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Pasos del formulario de solicitud (Requisitos → Confirmar): QScreenScaffold + QTopBar con flecha +
 * QStepIndicator(currentStep, totalSteps) al inicio del contenido + stickyAction
 * QButton(primaryActionText, fillMaxWidth) + SIN nav bar.
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QScreenScaffold, QTopBar, QStepIndicator, QButton.
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
    QScreenScaffold(
        modifier = modifier,
        topBar = {
            QTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
            )
        },
        stickyAction = {
            QButton(
                text = primaryActionText,
                onClick = onPrimaryAction,
                enabled = primaryActionEnabled,
                isLoading = isLoading,
                fillMaxWidth = true,
            )
        },
    ) {
        QStepIndicator(currentStep = currentStep, totalSteps = totalSteps)
        content()
    }
}

@Composable
private fun PreviewStep(
    currentStep: Int,
    primaryActionText: String = "Continuar",
    primaryActionEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    QarinoTheme {
        QFormStepTemplate(
            title = "Solicitud de adopción",
            subtitle = "Datos generales",
            currentStep = currentStep,
            totalSteps = 6,
            onBack = {},
            primaryActionText = primaryActionText,
            onPrimaryAction = {},
            primaryActionEnabled = primaryActionEnabled,
            isLoading = isLoading,
        ) {
            repeat(3) { index ->
                QCard(modifier = Modifier.fillMaxWidth()) {
                    QText(text = "Campo de ejemplo ${index + 1}")
                }
            }
        }
    }
}

@Preview(name = "Paso normal", showBackground = true, heightDp = 800)
@Composable
private fun QFormStepTemplatePreview() {
    PreviewStep(currentStep = 2)
}

@Preview(name = "Botón deshabilitado", showBackground = true, heightDp = 800)
@Composable
private fun QFormStepTemplateDisabledPreview() {
    PreviewStep(currentStep = 3, primaryActionEnabled = false)
}

@Preview(name = "Enviando (cargando)", showBackground = true, heightDp = 800)
@Composable
private fun QFormStepTemplateLoadingPreview() {
    PreviewStep(currentStep = 6, primaryActionText = "Enviar solicitud", isLoading = true)
}
