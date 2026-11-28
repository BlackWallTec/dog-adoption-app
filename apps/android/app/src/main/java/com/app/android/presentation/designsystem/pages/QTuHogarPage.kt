package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.useCases.useCasesSolicitudes.TuHogarErrors
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QCounter
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QLabeledRadioButton
import com.app.android.presentation.designsystem.molecules.QLabeledTextField
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val TOTAL_STEPS = 6
private const val SHORT_ANSWER_MAX_LENGTH = 50
private const val ANSWER_MAX_LENGTH = 100
private const val LONG_ANSWER_MAX_LENGTH = 250
private const val ANSWER_MIN_LINES = 3
private const val MIN_PEOPLE = 1

/**
 * Paso 3 de 6: preguntas sobre el hogar (mascotas, por qué adopta, dónde vivirá, personas en casa, tipo de
 * vivienda, patio, casero). Las preguntas Sí/No empiezan sin respuesta. Cada campo muestra su error debajo.
 * Dueño: P5 (Páginas)
 */
@Composable
fun QTuHogarPage(
    isLoading: Boolean,
    loadError: String?,
    data: TuHogar?,
    errors: TuHogarErrors?,
    onDataChange: (TuHogar) -> Unit,
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
        subtitle = "Tu hogar",
        currentStep = 3,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Continuar a Compromiso",
        onPrimaryAction = onContinue,
        modifier = modifier,
        primaryActionEnabled = data != null && !isLoading && loadError == null,
        isLoading = isSaving,
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = loadError,
            onRetry = onRetry,
            isEmpty = data == null,
        ) {
            if (data != null) {
                QText(text = "Cuéntanos dónde vivirá $petName y quiénes serán su nueva familia.")

                YesNoQuestion(
                    question = "¿Has tenido mascotas antes?",
                    answer = data.hadPet,
                    onAnswer = { onDataChange(data.copy(hadPet = it)) },
                    error = errors?.hadPet,
                )
                YesNoQuestion(
                    question = "¿Tienes otros animales en casa?",
                    answer = data.otherPets,
                    onAnswer = { onDataChange(data.copy(otherPets = it)) },
                    error = errors?.otherPets,
                )
                AnswerField(
                    label = "¿Por qué deseas adoptar?",
                    value = data.whyAdopt,
                    onValueChange = { onDataChange(data.copy(whyAdopt = it)) },
                    maxLength = ANSWER_MAX_LENGTH,
                    error = errors?.whyAdopt,
                )
                AnswerField(
                    label = "¿Dónde vivirá y dormirá el perrito?",
                    value = data.whereLive,
                    onValueChange = { onDataChange(data.copy(whereLive = it)) },
                    maxLength = ANSWER_MAX_LENGTH,
                    error = errors?.whereLive,
                )

                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QText(text = "¿Cuántas personas viven contigo?", style = QTextStyle.BodyStrong)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        QText(text = "Personas que viven contigo", modifier = Modifier.weight(1f))
                        QCounter(
                            value = data.peopleInHouse,
                            onDecrease = { onDataChange(data.copy(peopleInHouse = data.peopleInHouse - 1)) },
                            onIncrease = { onDataChange(data.copy(peopleInHouse = data.peopleInHouse + 1)) },
                            min = MIN_PEOPLE,
                        )
                    }
                    errors?.peopleInHouse?.let { QErrorMessage(text = it) }
                }

                YesNoQuestion(
                    question = "¿Todos están de acuerdo?",
                    answer = data.allAgree,
                    onAnswer = { onDataChange(data.copy(allAgree = it)) },
                    error = errors?.allAgree,
                )

                OptionsQuestion(
                    question = "Casa o departamento",
                    options = HouseType.entries.map { it.label() },
                    selectedIndex = data.houseType.ordinal,
                    onSelect = { onDataChange(data.copy(houseType = HouseType.entries[it])) },
                    error = errors?.houseType,
                )

                AnswerField(
                    label = "¿Tienes jardín o patio?",
                    value = data.hasGarden,
                    onValueChange = { onDataChange(data.copy(hasGarden = it)) },
                    maxLength = SHORT_ANSWER_MAX_LENGTH,
                    error = errors?.hasGarden,
                )
                YesNoQuestion(
                    question = "Si rentas, ¿tu casero acepta mascotas?",
                    answer = data.petAccept,
                    onAnswer = { onDataChange(data.copy(petAccept = it)) },
                    error = errors?.petAccept,
                )
                AnswerField(
                    label = "Cuéntanos sobre tus mascotas",
                    value = data.aboutPets,
                    onValueChange = { onDataChange(data.copy(aboutPets = it)) },
                    maxLength = LONG_ANSWER_MAX_LENGTH,
                    error = errors?.aboutPets,
                )

                if (saveError != null) {
                    QErrorMessage(text = saveError)
                }
            }
        }
    }
}

// Pregunta Sí/No. null = todavía sin responder (ninguna opción marcada).
@Composable
private fun YesNoQuestion(
    question: String,
    answer: Boolean?,
    onAnswer: (Boolean) -> Unit,
    error: String?,
) {
    OptionsQuestion(
        question = question,
        options = listOf("Sí", "No"),
        selectedIndex =
            when (answer) {
                true -> 0
                false -> 1
                null -> -1
            },
        onSelect = { onAnswer(it == 0) },
        error = error,
    )
}

// Pregunta de opción única con radios en fila. Si no caben en una línea, bajan a la siguiente.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionsQuestion(
    question: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    error: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xs)) {
        QText(text = question, style = QTextStyle.BodyStrong)
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            options.forEachIndexed { index, option ->
                QLabeledRadioButton(
                    text = option,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                )
            }
        }
        error?.let { QErrorMessage(text = it) }
    }
}

// Respuesta abierta de varias líneas con límite de caracteres.
@Composable
private fun AnswerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    error: String?,
) {
    QLabeledTextField(
        label = label,
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        placeholder = "Escribe tu respuesta...",
        modifier = Modifier.fillMaxWidth(),
        helperText = "${value.length}/$maxLength",
        errorText = error,
        singleLine = false,
        minLines = ANSWER_MIN_LINES,
    )
}

private fun HouseType.label(): String =
    when (this) {
        HouseType.CASA -> "Casa"
        HouseType.DEPARTAMENTO -> "Departamento"
        HouseType.OTRO -> "Otro"
    }

@Composable
private fun PreviewTuHogar(
    isLoading: Boolean = false,
    loadError: String? = null,
    errors: TuHogarErrors? = null,
) {
    QarinoTheme {
        QTuHogarPage(
            isLoading = isLoading,
            loadError = loadError,
            data = TuHogar(hadPet = true, whyAdopt = "Queremos darle un hogar a un perrito rescatado.", peopleInHouse = 3),
            errors = errors,
            onDataChange = {},
            isSaving = false,
            saveError = null,
            onRetry = {},
            onBack = {},
            onContinue = {},
            petName = "Firulais",
        )
    }
}

@Preview(showBackground = true, heightDp = 1700, name = "QTuHogarPage - normal")
@Composable
private fun QTuHogarPagePreview() {
    PreviewTuHogar()
}

@Preview(showBackground = true, heightDp = 1800, name = "QTuHogarPage - errores")
@Composable
private fun QTuHogarPageErrorsPreview() {
    PreviewTuHogar(errors = TuHogarErrors(otherPets = "Responde esta pregunta.", whereLive = "Cuéntanos dónde vivirá."))
}

@Preview(showBackground = true, heightDp = 800, name = "QTuHogarPage - cargando")
@Composable
private fun QTuHogarPageLoadingPreview() {
    PreviewTuHogar(isLoading = true)
}