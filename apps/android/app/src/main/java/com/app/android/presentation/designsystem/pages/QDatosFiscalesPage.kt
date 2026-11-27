package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.entities.entitiesDonativos.REGIMENES_FISCALES
import com.app.android.domain.useCases.useCasesDonativos.ErroresDatosFiscales
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QLabeledSelectField
import com.app.android.presentation.designsystem.molecules.QLabeledTextField
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.molecules.QUploadButton
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Datos fiscales para pedir CFDI: RFC, razón social, régimen (menú desplegable bajo el selector),
 * código postal, correo de facturación y constancia de situación fiscal. Guardar y Cancelar al
 * final. El resultado del guardado se muestra en la página (antes era un snackbar). Elegir y validar el
 * archivo de la constancia lo hace quien conecta la página (onPickCertificate).
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QStateContent, QLabeledTextField, QLabeledSelectField,
 * QUploadButton, QButton.
 * Reemplaza a: DatosFiscalesScreen (DatosFiscalesRoute).
 */
@Composable
fun QDatosFiscalesPage(
    isLoading: Boolean,
    loadError: String?,
    data: DatosFiscales?,
    hasSavedData: Boolean,
    errors: ErroresDatosFiscales?,
    onRfcChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onTaxRegimeChange: (regimeKey: String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onBillingEmailChange: (String) -> Unit,
    onPickCertificate: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    newCertificateName: String? = null,
    certificateInfo: String? = null,
    isSaving: Boolean = false,
    isSaved: Boolean = false,
    saveError: String? = null,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    var regimeOptionsOpen by remember { mutableStateOf(false) }
    val fieldErrors = errors ?: ErroresDatosFiscales()

    QDetailTemplate(
        title = "Datos fiscales",
        subtitle = "Guarda la información que usarás al solicitar un comprobante fiscal (CFDI).",
        onBack = onBack,
        selectedTab = QTab.Ajustes,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = loadError ?: if (data == null) "No se pudieron cargar tus datos fiscales." else null,
            onRetry = onRetry,
        ) {
            if (data != null) {
                if (!hasSavedData) {
                    QInfoMessage(
                        text = "Aún no has guardado tus datos fiscales. Consulta tu constancia para completarlos.",
                        important = true,
                    )
                }

                QLabeledTextField(
                    label = "RFC",
                    value = data.rfc,
                    onValueChange = onRfcChange,
                    placeholder = "Ingresa tu RFC",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = fieldErrors.rfc,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                )
                QLabeledTextField(
                    label = "Razón social",
                    value = data.razonSocial,
                    onValueChange = onBusinessNameChange,
                    placeholder = "Como aparece en tu constancia",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = fieldErrors.razonSocial,
                )

                QLabeledSelectField(
                    label = "Régimen fiscal",
                    value = regimeText(data.regimenFiscal),
                    onClick = { regimeOptionsOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Selecciona tu régimen",
                    errorText = fieldErrors.regimenFiscal,
                    expanded = regimeOptionsOpen,
                    onDismissRequest = { regimeOptionsOpen = false },
                    options = REGIMENES_FISCALES.map { (key, description) -> "$key · $description" },
                    onOptionSelected = { index ->
                        onTaxRegimeChange(REGIMENES_FISCALES[index].first)
                        regimeOptionsOpen = false
                    },
                )

                QLabeledTextField(
                    label = "Código postal",
                    value = data.codigoPostal,
                    onValueChange = onPostalCodeChange,
                    placeholder = "Código postal fiscal",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = fieldErrors.codigoPostal,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                QLabeledTextField(
                    label = "Correo de facturación",
                    value = data.correoFacturacion,
                    onValueChange = onBillingEmailChange,
                    placeholder = "correo@ejemplo.com",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = QIcons.Email,
                    errorText = fieldErrors.correoFacturacion,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                QSection(title = "Constancia de situación fiscal") {
                    val savedName = data.constanciaUrl?.substringAfterLast("/")
                    QUploadButton(
                        onClick = onPickCertificate,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Subir constancia",
                        helperText =
                            when {
                                newCertificateName != null -> certificateInfo
                                savedName != null -> "Guardada · Toca Cambiar para reemplazarla"
                                else -> "PDF, JPG o PNG · Máx. 10 MB"
                            },
                        fileName = newCertificateName ?: savedName,
                        enabled = !isSaving,
                    )
                    fieldErrors.constancia?.let { QErrorMessage(text = it) }
                }

                QText(
                    text = "Revisa que tu RFC, razón social y código postal coincidan con tu constancia.",
                    style = QTextStyle.Caption,
                )

                if (isSaved) {
                    QInfoMessage(text = "Tus datos fiscales se guardaron.", icon = QIcons.CheckCircle)
                }
                saveError?.let { QErrorMessage(text = it) }

                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
                    QButton(
                        text = "Guardar datos fiscales",
                        onClick = onSave,
                        isLoading = isSaving,
                        fillMaxWidth = true,
                    )
                    QButton(text = "Cancelar", onClick = onBack, enabled = !isSaving, fillMaxWidth = true)
                }
            }
        }
    }
}

// "626" -> "626 · Régimen Simplificado de Confianza". Una clave que no está en la lista se muestra tal cual.
private fun regimeText(key: String): String? {
    if (key.isBlank()) return null
    val description = REGIMENES_FISCALES.firstOrNull { it.first == key }?.second ?: return key
    return "$key · $description"
}

@Composable
private fun PreviewDatosFiscales(
    data: DatosFiscales? = DatosFiscales(rfc = "XAXX010101000", regimenFiscal = "626", codigoPostal = "76000"),
    hasSavedData: Boolean = false,
    errors: ErroresDatosFiscales? = null,
    isLoading: Boolean = false,
    isSaved: Boolean = false,
) {
    QarinoTheme {
        QDatosFiscalesPage(
            isLoading = isLoading,
            loadError = null,
            data = data,
            hasSavedData = hasSavedData,
            errors = errors,
            onRfcChange = {},
            onBusinessNameChange = {},
            onTaxRegimeChange = {},
            onPostalCodeChange = {},
            onBillingEmailChange = {},
            onPickCertificate = {},
            onSave = {},
            onRetry = {},
            onBack = {},
            onTabSelected = {},
            isSaved = isSaved,
        )
    }
}

@Preview(showBackground = true, heightDp = 1300, name = "QDatosFiscalesPage - sin guardar")
@Composable
private fun QDatosFiscalesPagePreview() {
    PreviewDatosFiscales()
}

@Preview(showBackground = true, heightDp = 1400, name = "QDatosFiscalesPage - errores")
@Composable
private fun QDatosFiscalesPageErrorsPreview() {
    PreviewDatosFiscales(
        hasSavedData = true,
        errors = ErroresDatosFiscales(razonSocial = "Escribe tu razón social.", constancia = "Sube tu constancia."),
    )
}

@Preview(showBackground = true, heightDp = 800, name = "QDatosFiscalesPage - error al cargar")
@Composable
private fun QDatosFiscalesPageLoadErrorPreview() {
    PreviewDatosFiscales(data = null)
}
