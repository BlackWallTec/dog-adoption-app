package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.CompromisoErrors
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QLabeledCheckbox
import com.app.android.presentation.designsystem.molecules.QLabeledRadioButton
import com.app.android.presentation.designsystem.molecules.QLabeledTextField
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelSolicitudes.Acceptances

private const val TOTAL_STEPS = 6
private const val ANSWER_MAX_LENGTH = 250
private const val ANSWER_MIN_LINES = 3

/**
 * Paso 4 de 6: preguntas sobre el cuidado de la mascota y los tres compromisos que hay que aceptar.
 * "Continuar a Documentos" se habilita cuando los tres compromisos están marcados.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QFormStepTemplate, QStateContent, QLabeledTextField, QLabeledRadioButton,
 * QLabeledCheckbox, QInfoMessage.
 * Reemplaza a: CompromisoScreen (CompromisoRoute).
 */
@Composable
fun QCompromisoPage(
    isLoading: Boolean,
    loadError: String?,
    data: Compromiso?,
    errors: CompromisoErrors?,
    onDataChange: (Compromiso) -> Unit,
    acceptances: Acceptances,
    onAcceptancesChange: (Acceptances) -> Unit,
    isSaving: Boolean,
    saveError: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    petName: String = "tu nueva mascota",
) {
    QFormStepTemplate(
        title = "Solicitud de adopción",
        subtitle = "Compromiso",
        currentStep = 4,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Continuar a Documentos",
        onPrimaryAction = onContinue,
        modifier = modifier,
        primaryActionEnabled = data != null && acceptances.allAccepted,
        isLoading = isSaving,
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = loadError,
            onRetry = onRetry,
            isEmpty = data == null,
        ) {
            if (data != null) {
                QText(
                    text =
                        "Cuéntanos cómo cuidarás a $petName. Tus respuestas nos ayudan a acompañarte " +
                            "en una adopción responsable.",
                )

                AnswerField(
                    label = "Menciona los cuidados básicos de una mascota",
                    value = data.petNeeds,
                    onValueChange = { onDataChange(data.copy(petNeeds = it)) },
                    placeholder = "Escribe los cuidados que le darías día a día...",
                    error = errors?.petNeeds,
                )
                AnswerField(
                    label = "¿Qué opinas de la esterilización?",
                    value = data.petSterilization,
                    onValueChange = { onDataChange(data.copy(petSterilization = it)) },
                    placeholder = "Cuéntanos qué opinas y por qué...",
                    error = errors?.petSterilization,
                )

                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xs)) {
                    QText(text = "¿Tienes un veterinario de confianza?", style = QTextStyle.BodyStrong)
                    Row(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(QSpacing.l),
                    ) {
                        QLabeledRadioButton(
                            text = "Sí",
                            selected = data.hasVet == true,
                            onClick = { onDataChange(data.copy(hasVet = true)) },
                        )
                        QLabeledRadioButton(
                            text = "No",
                            selected = data.hasVet == false,
                            onClick = { onDataChange(data.copy(hasVet = false)) },
                        )
                    }
                    errors?.hasVet?.let { QErrorMessage(text = it) }
                }

                AnswerField(
                    label = "Si tienes veterinario, ¿cuál es? Si no, ¿cómo piensas conseguir uno?",
                    value = data.vetDetail,
                    onValueChange = { onDataChange(data.copy(vetDetail = it)) },
                    placeholder = "Nombre del veterinario o clínica, o cómo lo buscarías...",
                    error = errors?.vetDetail,
                )
                AnswerField(
                    label =
                        "¿Cuántas horas al día estaría sola tu mascota y quién la cuidaría si viajas " +
                            "o sales de vacaciones?",
                    value = data.timeAlone,
                    onValueChange = { onDataChange(data.copy(timeAlone = it)) },
                    placeholder = "Indica cuántas horas y quién la cuidaría cuando no estés...",
                    error = errors?.timeAlone,
                )
                AnswerField(
                    label = "Describe un día normal para tu mascota (paseos, comida, juego, dónde duerme)",
                    value = data.dailyRoutine,
                    onValueChange = { onDataChange(data.copy(dailyRoutine = it)) },
                    placeholder = "Cuéntanos cómo sería su día, de la mañana a la noche...",
                    error = errors?.dailyRoutine,
                )
                AnswerField(
                    label =
                        "Si tu mascota ladra mucho, rompe cosas o se porta agresiva con otros perros, " +
                            "¿qué harías?",
                    value = data.dogManagement,
                    onValueChange = { onDataChange(data.copy(dogManagement = it)) },
                    placeholder = "Cuéntanos qué harías, paso a paso...",
                    error = errors?.dogManagement,
                )

                QSection(title = "Mis compromisos") {
                    QLabeledCheckbox(
                        text =
                            "Si en algún momento no puedo quedarme con mi mascota, me comprometo a devolverla " +
                                "a la organización y no regalarla, venderla ni abandonarla.",
                        checked = acceptances.acceptsReturn,
                        onCheckedChange = { onAcceptancesChange(acceptances.copy(acceptsReturn = it)) },
                    )
                    QLabeledCheckbox(
                        text =
                            "Entiendo que la adopción implica gastos continuos (alimento, vacunas, " +
                                "desparasitación y consultas veterinarias).",
                        checked = acceptances.petSpending,
                        onCheckedChange = { onAcceptancesChange(acceptances.copy(petSpending = it)) },
                    )
                    QLabeledCheckbox(
                        text = "Acepto que me den seguimiento (visitas o fotos) después de la adopción.",
                        checked = acceptances.petFollowing,
                        onCheckedChange = { onAcceptancesChange(acceptances.copy(petFollowing = it)) },
                    )
                }

                if (!acceptances.allAccepted) {
                    QInfoMessage(text = "Marca los tres compromisos para continuar.")
                }

                if (saveError != null) {
                    QErrorMessage(text = saveError)
                }
            }
        }
    }
}

// Respuesta abierta de varias líneas (máx. 250 caracteres) con contador como ayuda.
@Composable
private fun AnswerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
) {
    QLabeledTextField(
        label = label,
        value = value,
        onValueChange = { if (it.length <= ANSWER_MAX_LENGTH) onValueChange(it) },
        placeholder = placeholder,
        modifier = Modifier.fillMaxWidth(),
        helperText = "${value.length}/$ANSWER_MAX_LENGTH",
        errorText = error,
        singleLine = false,
        minLines = ANSWER_MIN_LINES,
    )
}

@Composable
private fun PreviewCompromiso(
    acceptances: Acceptances = Acceptances(acceptsReturn = true),
    errors: CompromisoErrors? = null,
    isLoading: Boolean = false,
) {
    QarinoTheme {
        QCompromisoPage(
            isLoading = isLoading,
            loadError = null,
            data = Compromiso(petNeeds = "Comida, agua limpia, paseos diarios y visitas al veterinario.", hasVet = true),
            errors = errors,
            onDataChange = {},
            acceptances = acceptances,
            onAcceptancesChange = {},
            isSaving = false,
            saveError = null,
            onRetry = {},
            onBack = {},
            onContinue = {},
            petName = "Firulais",
        )
    }
}

@Preview(showBackground = true, heightDp = 2000, name = "QCompromisoPage - normal")
@Composable
private fun QCompromisoPagePreview() {
    PreviewCompromiso()
}

@Preview(showBackground = true, heightDp = 2100, name = "QCompromisoPage - errores y todo aceptado")
@Composable
private fun QCompromisoPageErrorsPreview() {
    PreviewCompromiso(
        acceptances = Acceptances(acceptsReturn = true, petSpending = true, petFollowing = true),
        errors = CompromisoErrors(petSterilization = "Responde esta pregunta.", hasVet = "Elige una opción."),
    )
}

@Preview(showBackground = true, heightDp = 800, name = "QCompromisoPage - cargando")
@Composable
private fun QCompromisoPageLoadingPreview() {
    PreviewCompromiso(isLoading = true)
}
