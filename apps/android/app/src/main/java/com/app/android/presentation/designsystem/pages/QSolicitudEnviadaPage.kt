package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QPhoto
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QStatusChip
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QSuccessHeader
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.view.atomicDesign.page.MascotaResumen

private val PET_PHOTO_SIZE = 64.dp

/**
 * Éxito al enviar la solicitud de adopción: encabezado, tarjeta de la mascota con el folio y su estado
 * (En revisión) y botones para ver mis solicitudes o volver al catálogo (inicio del flujo).
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QSuccessHeader, QCard, QStatusChip, QButton.
 * Reemplaza a: EnviadaScreen (EnviadaRoute).
 */
@Composable
fun QSolicitudEnviadaPage(
    requestId: String,
    pet: MascotaResumen,
    onBack: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QDetailTemplate(
        title = "Solicitud de adopción",
        subtitle = null,
        onBack = onBack,
        selectedTab = QTab.Solicitudes,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QSuccessHeader(
            title = "¡Solicitud enviada!",
            subtitle = "Te notificaremos cuando haya novedades.",
            modifier = Modifier.padding(top = QSpacing.l),
        )

        QCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
            ) {
                QPhoto(
                    model = pet.fotoUrl,
                    contentDescription = "Foto de ${pet.nombre}",
                    modifier = Modifier.size(PET_PHOTO_SIZE),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(QSpacing.xxs),
                ) {
                    QText(text = pet.nombre, style = QTextStyle.Title, maxLines = 1)
                    QText(text = pet.detalle)
                    QText(text = "Solicitud $requestId", style = QTextStyle.Caption, maxLines = 1)
                    QStatusChip(text = "En revisión", status = QStatus.Pending, icon = QIcons.Clock)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            QButton(text = "Ver mis solicitudes", onClick = onMyRequestsClick, fillMaxWidth = true)
            QButton(text = "Volver al catálogo", onClick = onCatalogClick, fillMaxWidth = true)
        }
    }
}

@Preview(showBackground = true, heightDp = 800, name = "QSolicitudEnviadaPage")
@Composable
private fun QSolicitudEnviadaPagePreview() {
    QarinoTheme {
        QSolicitudEnviadaPage(
            requestId = "QA-9AAC227E",
            pet = MascotaResumen(nombre = "Firulais", detalle = "2 años · Macho · Mediano", raza = "Mestizo · Querétaro"),
            onBack = {},
            onMyRequestsClick = {},
            onCatalogClick = {},
            onTabSelected = {},
        )
    }
}
