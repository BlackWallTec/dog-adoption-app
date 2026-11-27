package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val BAR_HEIGHT = 6.dp

/**
 * Indicador de pasos del formulario de solicitud: totalSteps barritas, las completadas (incluido el
 * paso actual) en onAction, las demás en border, y el texto 'Paso X de N' (Caption). currentStep va de
 * 1 a totalSteps; si llega fuera de ese rango se ajusta.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText.
 * Reemplaza a: IndicadorPasos.
 */
@Composable
fun QStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val total = totalSteps.coerceAtLeast(1)
    val step = currentStep.coerceIn(1, total)
    Column(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.xs),
        ) {
            repeat(total) { index ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(BAR_HEIGHT)
                            .clip(QShapes.pill)
                            .background(if (index < step) QTheme.colors.onAction else QTheme.colors.border),
                )
            }
        }
        QText(text = "Paso $step de $total", style = QTextStyle.Caption, color = QTheme.colors.textSecondary)
    }
}

@Preview(showBackground = true, name = "QStepIndicator - pasos 1, 3 y 6 de 6")
@Composable
private fun QStepIndicatorPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xl),
        ) {
            QStepIndicator(currentStep = 1, totalSteps = 6)
            QStepIndicator(currentStep = 3, totalSteps = 6)
            QStepIndicator(currentStep = 6, totalSteps = 6)
            QStepIndicator(currentStep = 2, totalSteps = 3)
        }
    }
}