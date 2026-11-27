package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.domain.entities.entitiesVoluntariado.MarcaPrograma
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QStatusChip
import com.app.android.presentation.designsystem.atoms.QTag
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QCapacity
import com.app.android.presentation.designsystem.molecules.QIconLabel
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val COMMITMENT_NOTICE =
    "Al inscribirte te comprometes a asistir. Si no puedes venir, avisa al equipo con anticipación."

/**
 * Detalle de un programa de voluntariado: tipo (+ estado si ya no admite inscripciones), título, descripción,
 * tarjeta con fecha, horario, lugar y cupo, aviso de compromiso y botón fijo según la acción
 * (Inscribirme / Ya estás inscrito / Cupo lleno). Si el programa ya no existe ofrece volver a programas.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QSection, QCard, QCapacity, QInfoMessage, QButton, QStateContent.
 * Reemplaza a: DetalleProgramaPage (DetalleProgramaScreen).
 */
@Composable
fun QDetalleProgramaPage(
    isLoading: Boolean,
    errorMessage: String?,
    isNotFound: Boolean,
    type: String,
    title: String,
    description: String?,
    date: String,
    time: String,
    location: String,
    capacityText: String,
    capacityProgress: Float,
    action: AccionDetalle,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEnroll: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    typeDescription: String? = null,
    isEnrolling: Boolean = false,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    val showContent = !isLoading && errorMessage == null && !isNotFound
    val buttonState = action.buttonState()

    QDetailTemplate(
        title = "Detalle del programa",
        subtitle = null,
        onBack = onBack,
        selectedTab = QTab.Ayudar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        stickyAction =
            if (showContent && buttonState != null) {
                {
                    QButton(
                        text = buttonState.first,
                        onClick = onEnroll,
                        enabled = buttonState.second,
                        isLoading = isEnrolling,
                        fillMaxWidth = true,
                    )
                }
            } else {
                null
            },
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            isEmpty = isNotFound,
            emptyMessage = "Este programa ya no está disponible.",
            emptyActionText = "Volver a programas",
            onEmptyAction = onBack,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
                ) {
                    QTag(text = type)
                    if (action is AccionDetalle.SinAccion) {
                        QStatusChip(text = action.marca.label(), status = QStatus.Neutral, icon = QIcons.Info)
                    }
                }
                if (typeDescription != null) {
                    QText(text = typeDescription, style = QTextStyle.Caption)
                }
                QText(
                    text = title,
                    style = QTextStyle.Title,
                    modifier = Modifier.semantics { heading() },
                )
            }

            QSection(title = "Sobre el programa") {
                QText(text = description?.takeIf { it.isNotBlank() } ?: "Este programa no tiene descripción.")
            }

            QCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QIconLabel(icon = QIcons.Calendar, text = date, iconTint = QTheme.colors.textSecondary)
                    QIconLabel(icon = QIcons.Clock, text = time, iconTint = QTheme.colors.textSecondary)
                    QIconLabel(icon = QIcons.Location, text = location, iconTint = QTheme.colors.textSecondary)
                    QCapacity(text = capacityText, progress = capacityProgress, isFull = capacityProgress >= 1f)
                }
            }

            if (action !is AccionDetalle.SinAccion) {
                QInfoMessage(text = COMMITMENT_NOTICE)
            }
        }
    }
}

// Texto y si está habilitado el botón fijo; null = sin botón (programa cancelado, terminado, en curso…).
private fun AccionDetalle.buttonState(): Pair<String, Boolean>? =
    when (this) {
        AccionDetalle.Inscribirme -> "Inscribirme" to true
        AccionDetalle.YaInscrito -> "Ya estás inscrito" to false
        AccionDetalle.CupoLleno -> "Cupo lleno" to false
        is AccionDetalle.SinAccion -> null
    }

private fun MarcaPrograma.label(): String =
    when (this) {
        MarcaPrograma.CANCELADO -> "Cancelado"
        MarcaPrograma.FINALIZADO -> "Finalizado"
        MarcaPrograma.EN_CURSO -> "En curso"
        MarcaPrograma.NO_DISPONIBLE -> "No disponible"
    }

@Composable
private fun PreviewDetallePrograma(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isNotFound: Boolean = false,
    action: AccionDetalle = AccionDetalle.Inscribirme,
    description: String? = "Acompaña a los perros del refugio en un paseo tranquilo.\n\nTrae ropa cómoda, zapatos cerrados y agua.",
) {
    QarinoTheme {
        QDetalleProgramaPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            isNotFound = isNotFound,
            type = "Paseo",
            title = "Paseos con cariño",
            description = description,
            date = "Jue. 10 dic. 2026",
            time = "09:00 – 11:00 h",
            location = "Refugio Qariño · Querétaro",
            capacityText = "8/15 lugares ocupados · 7 disponibles",
            capacityProgress = 8f / 15f,
            action = action,
            onBack = {},
            onRetry = {},
            onEnroll = {},
            onTabSelected = {},
            typeDescription = "Paseos y socialización con los perros",
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QDetalleProgramaPage - inscribirme")
@Composable
private fun QDetalleProgramaPagePreview() {
    PreviewDetallePrograma()
}

@Preview(showBackground = true, heightDp = 900, name = "QDetalleProgramaPage - finalizado")
@Composable
private fun QDetalleProgramaPageFinishedPreview() {
    PreviewDetallePrograma(action = AccionDetalle.SinAccion(MarcaPrograma.FINALIZADO), description = null)
}

@Preview(showBackground = true, heightDp = 800, name = "QDetalleProgramaPage - no encontrado")
@Composable
private fun QDetalleProgramaPageNotFoundPreview() {
    PreviewDetallePrograma(isNotFound = true)
}

@Preview(showBackground = true, heightDp = 800, name = "QDetalleProgramaPage - cargando")
@Composable
private fun QDetalleProgramaPageLoadingPreview() {
    PreviewDetallePrograma(isLoading = true)
}
