package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QLabeledTextField
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.molecules.QUploadButton
import com.app.android.presentation.designsystem.organisms.QBankDataCard
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QMainTabTemplate
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Donar dinero (pestaña Donar): botón a "Mis donativos", monto y referencia (estado visual local mientras se
 * escriben), datos bancarios copiables, comprobante y botón fijo "Enviar donativo". Los errores de monto y
 * comprobante solo aparecen después del primer intento de envío. Elegir y validar el archivo lo hace quien
 * conecta la página (onPickReceipt); la página solo muestra el nombre o el error.
 *
 * Usa (obligatorio): QMainTabTemplate, QBankDataCard, QUploadButton, QLabeledTextField, QButton.
 * Reemplaza a: DonarDineroScreen (DonarDineroRoute).
 */
@Composable
fun QDonarDineroPage(
    bankData: List<Pair<String, String>>,
    receiptFileName: String?,
    onPickReceipt: () -> Unit,
    onCopy: (label: String, value: String) -> Unit,
    onSubmit: (amount: String, reference: String) -> Unit,
    onMyDonationsClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    receiptError: String? = null,
    onRemoveReceipt: (() -> Unit)? = null,
    isSending: Boolean = false,
    sendError: String? = null,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var reference by rememberSaveable { mutableStateOf("") }
    // No se marcan errores hasta que la persona intenta enviar.
    var triedToSend by rememberSaveable { mutableStateOf(false) }

    val cleanAmount = amount.replace("$", "").replace(",", "").trim()
    val isAmountValid = (cleanAmount.toDoubleOrNull() ?: 0.0) > 0.0
    val receiptMessage =
        receiptError ?: if (triedToSend && receiptFileName == null) "Adjunta tu comprobante para enviar el donativo." else null

    QMainTabTemplate(
        title = "Donar dinero",
        subtitle = "Realiza una transferencia y adjunta tu comprobante para registrar tu donativo.",
        selectedTab = QTab.Donar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        stickyAction = {
            QButton(
                text = "Enviar donativo",
                onClick = {
                    triedToSend = true
                    if (isAmountValid && receiptFileName != null) {
                        onSubmit(cleanAmount, reference.trim())
                    }
                },
                isLoading = isSending,
                fillMaxWidth = true,
            )
        },
    ) {
        QButton(text = "Ver mis donativos", onClick = onMyDonationsClick, fillMaxWidth = true)

        QLabeledTextField(
            label = "Monto donado",
            value = amount,
            onValueChange = { amount = it },
            placeholder = "$0 MXN",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = QIcons.Money,
            errorText = if (triedToSend && !isAmountValid) "Escribe el monto que transferiste." else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        QLabeledTextField(
            label = "Referencia bancaria (opcional)",
            value = reference,
            onValueChange = { reference = it },
            placeholder = "Folio o referencia de tu transferencia",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = QIcons.Hash,
            helperText = "Ayuda al refugio a encontrar tu transferencia más rápido.",
        )

        QSection(title = "Datos bancarios") {
            QBankDataCard(rows = bankData, onCopy = onCopy)
        }

        QSection(title = "Comprobante de transferencia") {
            QUploadButton(
                onClick = onPickReceipt,
                modifier = Modifier.fillMaxWidth(),
                text = "Subir comprobante",
                helperText = "Captura o PDF de tu transferencia · JPG, PNG o PDF · Máx. 10 MB",
                fileName = receiptFileName,
                onRemoveFile = onRemoveReceipt,
                enabled = !isSending,
            )
            receiptMessage?.let { QErrorMessage(text = it) }
        }

        sendError?.let { QErrorMessage(text = it) }
    }
}

private val previewBankData =
    listOf(
        "Banco" to "[BANCO]",
        "Titular" to "[TITULAR]",
        "CLABE" to "[CLABE]",
        "Cuenta" to "[CUENTA]",
    )

@Preview(showBackground = true, heightDp = 1300, name = "QDonarDineroPage - sin comprobante")
@Composable
private fun QDonarDineroPagePreview() {
    QarinoTheme {
        QDonarDineroPage(
            bankData = previewBankData,
            receiptFileName = null,
            onPickReceipt = {},
            onCopy = { _, _ -> },
            onSubmit = { _, _ -> },
            onMyDonationsClick = {},
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1300, name = "QDonarDineroPage - enviando con error")
@Composable
private fun QDonarDineroPageSendingPreview() {
    QarinoTheme {
        QDonarDineroPage(
            bankData = previewBankData,
            receiptFileName = "comprobante_transferencia_noviembre.pdf",
            onPickReceipt = {},
            onCopy = { _, _ -> },
            onSubmit = { _, _ -> },
            onMyDonationsClick = {},
            onTabSelected = {},
            onRemoveReceipt = {},
            isSending = true,
            sendError = "No pudimos registrar tu donativo. Intenta de nuevo.",
        )
    }
}
