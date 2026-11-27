package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QCheckItem
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QLabeledCheckbox
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

// Pasos del formulario: Requisitos, Datos generales, Tu hogar, Compromiso, Documentos, Confirmar.
private const val TOTAL_STEPS = 6

// (requisito, detalle) del paso 1, en el orden de la pantalla anterior.
private val REQUIREMENTS =
    listOf(
        "Ser mayor de edad" to "Tener 18 años o más para iniciar el proceso.",
        "Identificación oficial" to "INE o pasaporte vigente y legible.",
        "Comprobante de domicilio" to "Recibo reciente, no mayor a 3 meses.",
        "Compromiso de cuidado y seguimiento" to
            "Brindar alimento, atención veterinaria y un hogar seguro; aceptar el seguimiento del refugio.",
    )

/**
 * Paso 1 de 6 de la solicitud de adopción: lista de requisitos, aviso de esterilización y la casilla para
 * aceptarlos. "Continuar" se habilita al aceptar y muestra carga mientras se crea la solicitud.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QFormStepTemplate, QSection, QCheckItem, QInfoMessage, QLabeledCheckbox.
 * Reemplaza a: RequisitosScreen (RequisitosRoute).
 */
@Composable
fun QRequisitosPage(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QFormStepTemplate(
        title = "Solicitud de adopción",
        subtitle = "Requisitos",
        currentStep = 1,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Continuar",
        onPrimaryAction = onContinue,
        modifier = modifier,
        primaryActionEnabled = accepted,
        isLoading = isLoading,
    ) {
        QText(text = "Adoptar es un compromiso para toda la vida. Antes de seguir, revisa lo que necesitas.")

        QSection(title = "Lo que necesitas") {
            QCard(modifier = Modifier.fillMaxWidth()) {
                REQUIREMENTS.forEachIndexed { index, (title, detail) ->
                    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xxs)) {
                        QCheckItem(text = title)
                        // Alineado con el texto del requisito (después del ícono).
                        QText(
                            text = detail,
                            style = QTextStyle.Caption,
                            modifier = Modifier.padding(start = QSize.iconSmall + QSpacing.s),
                        )
                    }
                    if (index < REQUIREMENTS.lastIndex) {
                        QDivider(modifier = Modifier.padding(vertical = QSpacing.m))
                    }
                }
            }
        }

        QInfoMessage(
            title = "IMPORTANTE: La esterilización es requisito.",
            text =
                "Los adultos se entregan esterilizados; si es cachorro se firma una carta compromiso " +
                    "para esterilizarlo a los 4 meses.",
            important = true,
        )

        QLabeledCheckbox(
            text = "He leído y acepto los requisitos y las responsabilidades de una adopción responsable.",
            checked = accepted,
            onCheckedChange = onAcceptedChange,
        )

        if (errorMessage != null) {
            QErrorMessage(text = errorMessage)
        }
    }
}

@Preview(showBackground = true, heightDp = 1000, name = "QRequisitosPage - sin aceptar")
@Composable
private fun QRequisitosPagePreview() {
    QarinoTheme {
        QRequisitosPage(
            accepted = false,
            onAcceptedChange = {},
            isLoading = false,
            errorMessage = null,
            onBack = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1000, name = "QRequisitosPage - creando solicitud con error")
@Composable
private fun QRequisitosPageLoadingPreview() {
    QarinoTheme {
        QRequisitosPage(
            accepted = true,
            onAcceptedChange = {},
            isLoading = true,
            errorMessage = "No pudimos crear tu solicitud. Revisa tu conexión.",
            onBack = {},
            onContinue = {},
        )
    }
}
