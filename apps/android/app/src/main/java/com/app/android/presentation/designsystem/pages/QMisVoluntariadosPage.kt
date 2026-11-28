package com.app.android.presentation.designsystem.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.organisms.QConfirmDialog
import com.app.android.presentation.designsystem.organisms.QProgramCard
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelVoluntariado.AccionPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi

/**
 * "Mis Programas": programas vigentes en los que la persona está inscrita. Cada tarjeta permite cancelar
 * la inscripción (si el programa no ha empezado) después de confirmar en un diálogo. `message` muestra un
 * aviso (por ejemplo "Inscripción cancelada") arriba de la lista.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QProgramCard, QConfirmDialog, QStateContent.
 * Reemplaza a: ProgramasPage en modo "Mis programas" (MisProgramasScreen).
 */
@Composable
fun QMisVoluntariadosPage(
    isLoading: Boolean,
    errorMessage: String?,
    programs: List<ProgramaUi>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onProgramClick: (programId: String) -> Unit,
    onCancelEnrollment: (programId: String) -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
    message: String? = null,
) {
    // Programa que espera confirmación para cancelar (null = diálogo cerrado).
    var programToCancel by remember { mutableStateOf<ProgramaUi?>(null) }

    QDetailTemplate(
        title = "Mis Programas",
        subtitle = "Estas son tus próximas jornadas agendadas.",
        onBack = onBack,
        selectedTab = QTab.Ayudar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        if (message != null) {
            QInfoMessage(text = message)
        }

        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            isEmpty = programs.isEmpty(),
            emptyMessage = "Aún no tienes voluntariados agendados.",
        ) {
            programs.forEach { program ->
                val canCancel = program.accion == AccionPrograma.CANCELAR
                QProgramCard(
                    type = program.tipo,
                    title = program.titulo,
                    date = program.fecha,
                    time = program.horario,
                    location = program.ubicacion,
                    capacityText = program.textoCupo,
                    capacityProgress = program.progresoCupo,
                    onClick = { onProgramClick(program.id) },
                    isFull = program.progresoCupo >= 1f,
                    enrolled = true,
                    onCancel = if (canCancel) ({ programToCancel = program }) else null,
                    // Mientras se cancela, el ViewModel pone "Cancelando…" y apaga el botón.
                    cancelText = if (program.botonHabilitado) "Cancelar inscripción" else program.textoBoton,
                    cancelEnabled = program.botonHabilitado,
                )
            }
        }
    }

    programToCancel?.let { program ->
        QConfirmDialog(
            title = "¿Cancelar tu inscripción?",
            description = "Si cancelas, liberarás tu lugar en ${program.titulo}. ¿Quieres continuar?",
            confirmText = "Sí, cancelar",
            onConfirm = {
                onCancelEnrollment(program.id)
                programToCancel = null
            },
            onDismiss = { programToCancel = null },
            destructive = true,
        )
    }
}

private val previewMyPrograms =
    listOf(
        ProgramaUi(
            id = "1",
            tipo = "Voluntariado",
            titulo = "Paseos y socialización de fin de semana",
            fecha = "Dom. 15 nov. 2026",
            horario = "09:00 – 14:00 h",
            ubicacion = "Refugio Qariño · Querétaro",
            textoCupo = "12/25 lugares ocupados · 13 disponibles",
            progresoCupo = 12f / 25f,
            textoBoton = "Cancelar",
            botonHabilitado = true,
            accion = AccionPrograma.CANCELAR,
        ),
    )

@Composable
private fun PreviewMisVoluntariados(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    programs: List<ProgramaUi> = previewMyPrograms,
    message: String? = null,
) {
    QarinoTheme {
        QMisVoluntariadosPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            programs = programs,
            onBack = {},
            onRetry = {},
            onProgramClick = {},
            onCancelEnrollment = {},
            onTabSelected = {},
            message = message,
        )
    }
}

@Preview(showBackground = true, heightDp = 800, name = "QMisVoluntariadosPage - lista (diálogo: botón Run)")
@Composable
private fun QMisVoluntariadosPagePreview() {
    PreviewMisVoluntariados()
}

@Preview(showBackground = true, heightDp = 800, name = "QMisVoluntariadosPage - con aviso")
@Composable
private fun QMisVoluntariadosPageMessagePreview() {
    PreviewMisVoluntariados(message = "Inscripción cancelada.")
}

@Preview(showBackground = true, heightDp = 800, name = "QMisVoluntariadosPage - vacío")
@Composable
private fun QMisVoluntariadosPageEmptyPreview() {
    PreviewMisVoluntariados(programs = emptyList())
}

@Preview(showBackground = true, heightDp = 800, name = "QMisVoluntariadosPage - error")
@Composable
private fun QMisVoluntariadosPageErrorPreview() {
    PreviewMisVoluntariados(errorMessage = "No se pudieron cargar tus programas")
}