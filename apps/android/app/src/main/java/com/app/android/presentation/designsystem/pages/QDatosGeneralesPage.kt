package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QLabeledTextField
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val TOTAL_STEPS = 6
private const val OCCUPATION_MAX_LENGTH = 100

/**
 * Paso 2 de 6: datos generales precargados del perfil (solo lectura) y la ocupación, que es el único campo
 * editable. Maneja la carga de los datos (cargando / error con Reintentar) y el guardado al continuar.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QFormStepTemplate, QStateContent, QSection, QCard, QLabeledTextField.
 * Reemplaza a: DatosGeneralesScreen (DatosGeneralesRoute).
 */
@Composable
fun QDatosGeneralesPage(
    isLoading: Boolean,
    loadError: String?,
    data: DatosGenerales?,
    age: Int?,
    occupationError: String?,
    onOccupationChange: (String) -> Unit,
    isSaving: Boolean,
    saveError: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QFormStepTemplate(
        title = "Solicitud de adopción",
        subtitle = "Datos generales",
        currentStep = 2,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Continuar a Tu hogar",
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
                QText(
                    text = "Precargamos los datos de tu perfil. Revísalos y completa tu ocupación antes de continuar.",
                )

                QSection(title = "Datos de tu perfil") {
                    ReadOnlyCard(
                        fields =
                            listOf(
                                Triple("Nombre", data.nombreCompleto, null),
                                Triple("Edad", age?.let { "$it años" }.orEmpty(), birthText(data.fechaNacimiento)),
                                Triple("Domicilio", data.domicilio, null),
                                Triple("Delegación/Municipio", data.municipio, null),
                                Triple("Teléfono", data.telefono, null),
                                Triple("Correo", data.correo, null),
                            ),
                    )
                }

                QLabeledTextField(
                    label = "Ocupación",
                    value = data.ocupacion,
                    onValueChange = { if (it.length <= OCCUPATION_MAX_LENGTH) onOccupationChange(it) },
                    placeholder = "Escribe tu ocupación",
                    modifier = Modifier.fillMaxWidth(),
                    helperText = "Este dato no está guardado en tu perfil.",
                    errorText = occupationError,
                )

                if (saveError != null) {
                    QErrorMessage(text = saveError)
                }
            }
        }
    }
}

// Datos de solo lectura: (etiqueta, valor, ayuda opcional). Un valor vacío se muestra como "—".
@Composable
private fun ReadOnlyCard(fields: List<Triple<String, String, String?>>) {
    QCard(modifier = Modifier.fillMaxWidth()) {
        fields.forEachIndexed { index, (label, value, helper) ->
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(QSpacing.xxs),
            ) {
                QText(text = label, style = QTextStyle.Caption)
                QText(text = value.ifBlank { "—" }, style = QTextStyle.BodyStrong)
                if (helper != null) {
                    QText(text = helper, style = QTextStyle.Caption)
                }
            }
            if (index < fields.lastIndex) {
                QDivider(modifier = Modifier.padding(vertical = QSpacing.m))
            }
        }
    }
}

// "1995-03-15" -> "Nacimiento: 15/03/1995". Null si no viene o no tiene ese formato.
private fun birthText(date: String?): String? {
    if (date.isNullOrBlank()) return null
    val parts = date.take(10).split("-")
    if (parts.size != 3) return null
    return "Nacimiento: ${parts[2]}/${parts[1]}/${parts[0]}"
}

private val previewData =
    DatosGenerales(
        nombreCompleto = "Persona Ejemplo",
        fechaNacimiento = "1995-03-15",
        ocupacion = "",
        domicilio = "Calle Ejemplo 123, Col. Centro, Querétaro, Qro. · C.P. 76000",
        municipio = "Querétaro",
        telefono = "442 000 0000",
        correo = "correo@ejemplo.com",
    )

@Composable
private fun PreviewDatosGenerales(
    isLoading: Boolean = false,
    loadError: String? = null,
    occupationError: String? = null,
    saveError: String? = null,
) {
    QarinoTheme {
        QDatosGeneralesPage(
            isLoading = isLoading,
            loadError = loadError,
            data = previewData,
            age = 31,
            occupationError = occupationError,
            onOccupationChange = {},
            isSaving = false,
            saveError = saveError,
            onRetry = {},
            onBack = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "QDatosGeneralesPage - normal")
@Composable
private fun QDatosGeneralesPagePreview() {
    PreviewDatosGenerales()
}

@Preview(showBackground = true, heightDp = 1100, name = "QDatosGeneralesPage - errores")
@Composable
private fun QDatosGeneralesPageErrorsPreview() {
    PreviewDatosGenerales(occupationError = "Escribe tu ocupación.", saveError = "No pudimos guardar tus datos.")
}

@Preview(showBackground = true, heightDp = 800, name = "QDatosGeneralesPage - error al cargar")
@Composable
private fun QDatosGeneralesPageLoadErrorPreview() {
    PreviewDatosGenerales(loadError = "No pudimos cargar tus datos.")
}
