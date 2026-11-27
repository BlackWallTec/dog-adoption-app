package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QButtonSize
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QStatusChip
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QIconLabel
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Mis donativos: lista de transferencias registradas con monto, fecha y estado. Si el donativo ya está
 * validado se puede pedir el CFDI una sola vez (botón con carga mientras se envía). Aviso opcional arriba.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QStateContent, QCard, QStatusChip, QInfoMessage, QButton.
 * Reemplaza a: MisDonativosScreen (MisDonativosRoute).
 */
@Composable
fun QMisDonativosPage(
    isLoading: Boolean,
    errorMessage: String?,
    donations: List<Donativo>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRequestCfdi: (donationId: String) -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    sendingCfdiId: String? = null,
    infoMessage: String? = null,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QDetailTemplate(
        title = "Mis donativos",
        subtitle = "Consulta el estado de tus transferencias.",
        onBack = onBack,
        selectedTab = QTab.Donar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        scrollable = false,
    ) {
        infoMessage?.let { QInfoMessage(text = it) }

        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            modifier = Modifier.weight(1f),
            isEmpty = donations.isEmpty(),
            emptyMessage = "Todavía no tienes donativos.",
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(QSpacing.m),
            ) {
                items(items = donations, key = { it.id }) { donation ->
                    DonationCard(
                        donation = donation,
                        isSendingCfdi = sendingCfdiId == donation.id,
                        onRequestCfdi = { onRequestCfdi(donation.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DonationCard(
    donation: Donativo,
    isSendingCfdi: Boolean,
    onRequestCfdi: () -> Unit,
) {
    QCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
            ) {
                QText(
                    text = "$" + "%,.2f".format(donation.monto) + " MXN",
                    style = QTextStyle.Title,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                QStatusChip(text = donation.estado, status = donation.estado.toStatus())
            }
            QIconLabel(
                icon = QIcons.Calendar,
                text = donation.fecha,
                iconTint = QTheme.colors.textSecondary,
                style = QTextStyle.Caption,
            )
            if (donation.cfdiSolicitado) {
                QIconLabel(
                    icon = QIcons.CheckCircle,
                    text = "CFDI solicitado",
                    iconTint = QTheme.colors.onPositive,
                    style = QTextStyle.BodyStrong,
                )
            } else if (donation.puedePedirCfdi) {
                QButton(
                    text = "Pedir CFDI",
                    onClick = onRequestCfdi,
                    size = QButtonSize.Small,
                    icon = QIcons.Document,
                    enabled = !isSendingCfdi,
                    isLoading = isSendingCfdi,
                    fillMaxWidth = true,
                )
            }
        }
    }
}

// El estado llega como texto desde la base de datos ("Validado", "Pendiente", "Rechazado"…).
private fun String.toStatus(): QStatus =
    when {
        equals("Validado", ignoreCase = true) -> QStatus.Positive
        startsWith("Rechaz", ignoreCase = true) -> QStatus.Negative
        startsWith("Pendiente", ignoreCase = true) -> QStatus.Pending
        else -> QStatus.Neutral
    }

private val previewDonations =
    listOf(
        Donativo(id = "1", monto = 500.0, fecha = "2026-11-20", estado = "Validado", cfdiSolicitado = false),
        Donativo(id = "2", monto = 1250.5, fecha = "2026-11-02", estado = "Validado", cfdiSolicitado = true),
        Donativo(id = "3", monto = 200.0, fecha = "2026-10-15", estado = "Pendiente", cfdiSolicitado = false),
        Donativo(id = "4", monto = 150.0, fecha = "2026-09-30", estado = "Rechazado", cfdiSolicitado = false),
    )

@Composable
private fun PreviewMisDonativos(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    donations: List<Donativo> = previewDonations,
    infoMessage: String? = null,
) {
    QarinoTheme {
        QMisDonativosPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            donations = donations,
            onBack = {},
            onRetry = {},
            onRequestCfdi = {},
            onTabSelected = {},
            infoMessage = infoMessage,
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QMisDonativosPage - lista")
@Composable
private fun QMisDonativosPagePreview() {
    PreviewMisDonativos(infoMessage = "Solicitamos tu CFDI. Te llegará a tu correo de facturación.")
}

@Preview(showBackground = true, heightDp = 800, name = "QMisDonativosPage - vacío")
@Composable
private fun QMisDonativosPageEmptyPreview() {
    PreviewMisDonativos(donations = emptyList())
}

@Preview(showBackground = true, heightDp = 800, name = "QMisDonativosPage - error")
@Composable
private fun QMisDonativosPageErrorPreview() {
    PreviewMisDonativos(errorMessage = "No pudimos cargar tus donativos.")
}
