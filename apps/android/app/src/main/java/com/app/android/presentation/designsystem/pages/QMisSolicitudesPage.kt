package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QRequestCard
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QMainTabTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelSolicitudes.SolicitudItem

/**
 * Mis solicitudes (pestaña Solicitudes): lista de solicitudes de adopción con su estado. Cada tarjeta se
 * expande para mostrar qué significa el estado y el comentario del equipo; si la solicitud se puede
 * corregir aparece "Editar". Sin solicitudes invita a ir al catálogo.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QMainTabTemplate, QRequestCard, QStateContent.
 * Reemplaza a: MisSolicitudesScreen (MisSolicitudesRoute).
 */
@Composable
fun QMisSolicitudesPage(
    isLoading: Boolean,
    errorMessage: String?,
    requests: List<SolicitudItem>,
    onRetry: () -> Unit,
    onEditClick: (requestId: String) -> Unit,
    onCatalogClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QMainTabTemplate(
        title = "Mis solicitudes",
        subtitle = "Consulta el estado de tus solicitudes de adopción.",
        selectedTab = QTab.Solicitudes,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        scrollable = false,
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            modifier = Modifier.weight(1f),
            isEmpty = requests.isEmpty(),
            emptyMessage = "Aún no tienes solicitudes.\n¡Encuentra a tu nuevo amigo en el catálogo!",
            emptyActionText = "Ver catálogo",
            onEmptyAction = onCatalogClick,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(QSpacing.m),
            ) {
                items(items = requests, key = { it.idSolicitud }) { request ->
                    // Abierta/cerrada es solo de la pantalla; rememberSaveable la conserva al girar el teléfono.
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    val status = request.estado.toStatusUi()
                    QRequestCard(
                        photo = request.fotoUrl,
                        name = request.nombrePerro,
                        folio = request.folio,
                        date = request.fecha,
                        statusText = status.text,
                        status = status.status,
                        expanded = expanded,
                        onToggleDetails = { expanded = !expanded },
                        statusIcon = status.icon,
                        onEdit = if (request.puedeEditar) ({ onEditClick(request.idSolicitud) }) else null,
                    ) {
                        RequestDetails(message = status.message, adminComment = request.comentarioAdmin)
                    }
                }
            }
        }
    }
}

// Lo que se despliega con "Ver detalles": qué significa el estado + comentario del equipo.
@Composable
private fun RequestDetails(
    message: String,
    adminComment: String?,
) {
    QText(text = message)
    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.xxs)) {
        QText(text = "Comentarios del administrador", style = QTextStyle.BodyStrong)
        QText(
            text = adminComment?.takeIf { it.isNotBlank() } ?: "Sin comentarios por ahora.",
            style = QTextStyle.Caption,
        )
    }
}

private class RequestStatusUi(
    val text: String,
    val status: QStatus,
    val icon: ImageVector,
    val message: String,
)

private fun EstadoSolicitud.toStatusUi(): RequestStatusUi =
    when (this) {
        EstadoSolicitud.PENDIENTE_REVISION -> {
            RequestStatusUi(
                text = "En revisión",
                status = QStatus.Pending,
                icon = QIcons.Clock,
                message = "Tu solicitud está en revisión, pronto nos contactaremos contigo.",
            )
        }

        EstadoSolicitud.EN_PERIODO_PRUEBA -> {
            RequestStatusUi(
                text = "En periodo de prueba",
                status = QStatus.Pending,
                icon = QIcons.Paw,
                message =
                    "Tu solicitud fue aceptada y tu mascota está contigo en periodo de prueba. " +
                        "Te daremos seguimiento durante este tiempo.",
            )
        }

        EstadoSolicitud.APROBADA -> {
            RequestStatusUi(
                text = "Aprobada",
                status = QStatus.Positive,
                icon = QIcons.Check,
                message = "¡Felicidades! Tu adopción fue aprobada de forma definitiva.",
            )
        }

        EstadoSolicitud.RECHAZADA -> {
            RequestStatusUi(
                text = "Rechazada",
                status = QStatus.Negative,
                icon = QIcons.Close,
                message = "Tu solicitud no fue aprobada. Revisa los comentarios del equipo para más información.",
            )
        }

        EstadoSolicitud.DESCONOCIDO -> {
            RequestStatusUi(
                text = "En proceso",
                status = QStatus.Neutral,
                icon = QIcons.Info,
                message = "Tu solicitud está en proceso.",
            )
        }
    }

private val previewRequests =
    listOf(
        SolicitudItem("1", "Firulais", null, "QA-0248", "12 oct 2026", EstadoSolicitud.PENDIENTE_REVISION, false, null),
        SolicitudItem(
            "2",
            "Canela",
            null,
            "QA-0231",
            "10 oct 2026",
            EstadoSolicitud.RECHAZADA,
            true,
            "La foto de tu identificación sale borrosa, súbela de nuevo por favor.",
        ),
        SolicitudItem("3", "Chispa", null, "QA-0210", "28 sep 2026", EstadoSolicitud.APROBADA, false, null),
        SolicitudItem("4", "Pelusa", null, "QA-0185", "20 sep 2026", EstadoSolicitud.EN_PERIODO_PRUEBA, false, null),
    )

@Composable
private fun PreviewMisSolicitudes(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    requests: List<SolicitudItem> = previewRequests,
) {
    QarinoTheme {
        QMisSolicitudesPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            requests = requests,
            onRetry = {},
            onEditClick = {},
            onCatalogClick = {},
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "QMisSolicitudesPage - lista")
@Composable
private fun QMisSolicitudesPagePreview() {
    PreviewMisSolicitudes()
}

@Preview(showBackground = true, heightDp = 800, name = "QMisSolicitudesPage - vacío")
@Composable
private fun QMisSolicitudesPageEmptyPreview() {
    PreviewMisSolicitudes(requests = emptyList())
}

@Preview(showBackground = true, heightDp = 800, name = "QMisSolicitudesPage - error")
@Composable
private fun QMisSolicitudesPageErrorPreview() {
    PreviewMisSolicitudes(errorMessage = "No pudimos cargar tus solicitudes. Revisa tu conexión.")
}

@Preview(showBackground = true, heightDp = 800, name = "QMisSolicitudesPage - cargando")
@Composable
private fun QMisSolicitudesPageLoadingPreview() {
    PreviewMisSolicitudes(isLoading = true)
}
