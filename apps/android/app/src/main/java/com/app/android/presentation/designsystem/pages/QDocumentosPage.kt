package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.molecules.QUploadButton
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DocumentSlot
import com.app.android.presentation.viewmodel.viewModelSolicitudes.UploadDocumentsUiState

private const val TOTAL_STEPS = 6

/**
 * Paso 5 de 6: subir identificación oficial y comprobante de domicilio. Cada documento muestra el nombre del
 * archivo elegido (con "Cambiar") o el botón para subirlo. "Continuar" se habilita con los dos documentos.
 * Elegir el archivo lo hace quien conecta la página (onPickFile abre el selector de archivos).
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QFormStepTemplate, QSection, QUploadButton, QInfoMessage.
 * Reemplaza a: UploadDocuments (UploadDocumentsRoute).
 */
@Composable
fun QDocumentosPage(
    state: UploadDocumentsUiState,
    onPickFile: (DocumentType) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QFormStepTemplate(
        title = "Solicitud de adopción",
        subtitle = "Documentos",
        currentStep = 5,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Continuar",
        onPrimaryAction = onContinue,
        modifier = modifier,
        primaryActionEnabled = state.canContinue,
        isLoading = state.isSubmittingAll,
    ) {
        QText(text = "Adjunta tus documentos. La información se usará únicamente para tu proceso de adopción.")

        DocumentSection(
            title = "Identificación oficial",
            buttonText = "Subir identificación",
            slot = state.slots[DocumentType.INE] ?: DocumentSlot(),
            onPick = { onPickFile(DocumentType.INE) },
        )
        DocumentSection(
            title = "Comprobante de domicilio",
            buttonText = "Subir comprobante",
            slot = state.slots[DocumentType.DOMICILIO] ?: DocumentSlot(),
            onPick = { onPickFile(DocumentType.DOMICILIO) },
        )

        QText(
            text = "PDF, JPG o PNG · Máximo 5 MB por archivo. Asegúrate de que los datos sean legibles.",
            style = QTextStyle.Caption,
        )

        state.errorMessage?.let { QErrorMessage(text = it) }

        QInfoMessage(
            text = "Tus documentos están protegidos y solo los revisará el equipo de adopciones.",
            icon = QIcons.Lock,
        )
    }
}

@Composable
private fun DocumentSection(
    title: String,
    buttonText: String,
    slot: DocumentSlot,
    onPick: () -> Unit,
) {
    QSection(title = title) {
        QUploadButton(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            text = buttonText,
            helperText = slot.fileInfo.takeIf { slot.isLoaded && it.isNotBlank() },
            fileName = slot.fileName,
            isLoading = slot.isUploading,
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QDocumentosPage - uno subido")
@Composable
private fun QDocumentosPagePreview() {
    QarinoTheme {
        QDocumentosPage(
            state =
                UploadDocumentsUiState(
                    slots =
                        mapOf(
                            DocumentType.INE to DocumentSlot(fileName = "identificacion.pdf", fileInfo = "1.2 MB · Listo"),
                            DocumentType.DOMICILIO to DocumentSlot(),
                        ),
                ),
            onPickFile = {},
            onBack = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QDocumentosPage - subiendo con error")
@Composable
private fun QDocumentosPageUploadingPreview() {
    QarinoTheme {
        QDocumentosPage(
            state =
                UploadDocumentsUiState(
                    slots =
                        mapOf(
                            DocumentType.INE to DocumentSlot(fileName = "identificacion.pdf", fileInfo = "1.2 MB · Listo"),
                            DocumentType.DOMICILIO to DocumentSlot(isUploading = true),
                        ),
                    errorMessage = "El archivo pesa más de 5 MB.",
                ),
            onPickFile = {},
            onBack = {},
            onContinue = {},
        )
    }
}
