package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.organisms.QConfirmDialog
import com.app.android.presentation.designsystem.organisms.QProgramCard
import com.app.android.presentation.designsystem.organisms.QSuccessHeader
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Confirmación después de inscribirse a un programa: encabezado de éxito, tarjeta del programa con opción
 * de cancelar (pide confirmación) y botones para ver mis voluntariados o seguir explorando programas.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QSuccessHeader, QProgramCard, QConfirmDialog, QButton.
 * Reemplaza a: VolunteeringConfirmationScreen (ConfirmacionInscripcionRoute).
 */
@Composable
fun QConfirmacionInscripcionPage(
    type: String,
    title: String,
    date: String,
    time: String,
    location: String,
    capacityText: String,
    capacityProgress: Float,
    onBack: () -> Unit,
    onProgramClick: () -> Unit,
    onCancelEnrollment: () -> Unit,
    onMyVolunteeringClick: () -> Unit,
    onExploreProgramsClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    var confirmingCancel by remember { mutableStateOf(false) }

    QDetailTemplate(
        title = "Voluntariado",
        subtitle = null,
        onBack = onBack,
        selectedTab = QTab.Ayudar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QSuccessHeader(
            title = "¡Te inscribiste!",
            subtitle = "Gracias por sumar tu cariño. Tu lugar está confirmado.",
            modifier = Modifier.padding(top = QSpacing.l),
        )
        QProgramCard(
            type = type,
            title = title,
            date = date,
            time = time,
            location = location,
            capacityText = capacityText,
            capacityProgress = capacityProgress,
            onClick = onProgramClick,
            isFull = capacityProgress >= 1f,
            enrolled = true,
            onCancel = { confirmingCancel = true },
        )
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            QButton(text = "Ver mis programas", onClick = onMyVolunteeringClick, fillMaxWidth = true)
            QButton(text = "Explorar programas", onClick = onExploreProgramsClick, fillMaxWidth = true)
        }
    }

    if (confirmingCancel) {
        QConfirmDialog(
            title = "¿Cancelar tu inscripción?",
            description = "Si cancelas, liberarás tu lugar en $title. ¿Quieres continuar?",
            confirmText = "Sí, cancelar",
            onConfirm = {
                confirmingCancel = false
                onCancelEnrollment()
            },
            onDismiss = { confirmingCancel = false },
            destructive = true,
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QConfirmacionInscripcionPage (diálogo: botón Run)")
@Composable
private fun QConfirmacionInscripcionPagePreview() {
    QarinoTheme {
        QConfirmacionInscripcionPage(
            type = "Voluntariado",
            title = "Paseos y socialización de fin de semana",
            date = "Dom. 15 nov. 2026",
            time = "09:00 – 14:00 h",
            location = "Refugio Qariño · Querétaro",
            capacityText = "1/25 lugares ocupados · 24 disponibles",
            capacityProgress = 1f / 25f,
            onBack = {},
            onProgramClick = {},
            onCancelEnrollment = {},
            onMyVolunteeringClick = {},
            onExploreProgramsClick = {},
            onTabSelected = {},
        )
    }
}
