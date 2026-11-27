package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.organisms.QSuccessHeader
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Éxito al registrar un donativo: encabezado, tarjeta con el monto (pendiente de validación), aviso de dónde
 * consultar el estado y botones para volver a Donar (inicio del flujo) o ver mis donativos.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QSuccessHeader, QCard, QInfoMessage, QButton.
 * Reemplaza a: DonativoEnviadoScreen.
 */
@Composable
fun QDonativoEnviadoPage(
    amount: String,
    onBack: () -> Unit,
    onDonateAgainClick: () -> Unit,
    onMyDonationsClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QDetailTemplate(
        title = "Donativo",
        subtitle = null,
        onBack = onBack,
        selectedTab = QTab.Donar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QSuccessHeader(
            title = "Registramos tu donativo, lo revisaremos pronto.",
            subtitle = "Estamos revisando tu transferencia. Te avisaremos cuando esté confirmada.",
            modifier = Modifier.padding(top = QSpacing.l),
        )

        QCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
            ) {
                QText(text = "Donativo registrado", style = QTextStyle.Label)
                QText(text = amount, style = QTextStyle.TopBarTitle)
                QText(text = "Transferencia · Pendiente de validación", style = QTextStyle.Caption)
            }
        }

        QInfoMessage(text = "Puedes consultar el estado de tu transferencia en Mis donativos.")

        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            QButton(text = "Volver a Donar", onClick = onDonateAgainClick, fillMaxWidth = true)
            QButton(text = "Ver mis donativos", onClick = onMyDonationsClick, fillMaxWidth = true)
        }
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QDonativoEnviadoPage")
@Composable
private fun QDonativoEnviadoPagePreview() {
    QarinoTheme {
        QDonativoEnviadoPage(
            amount = "$500.00 MXN",
            onBack = {},
            onDonateAgainClick = {},
            onMyDonationsClick = {},
            onTabSelected = {},
        )
    }
}
