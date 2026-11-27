package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QLoadingIndicator
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QStateMessage
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Envuelve el contenido según el estado: isLoading -> QLoadingIndicator centrado; errorMessage != null
 * -> QStateMessage(errorMessage, QIcons.Error, 'Reintentar', onRetry); isEmpty ->
 * QStateMessage(emptyMessage, actionText = emptyActionText, onAction = onEmptyAction); si no ->
 * content().
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QLoadingIndicator, QStateMessage.
 * Reemplaza a: EstadoCargando, EstadoError.
 */
@Composable
fun QStateContent(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
    emptyMessage: String = "No hay información por ahora.",
    emptyActionText: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    when {
        isLoading -> {
            CenteredState(modifier = modifier) {
                QLoadingIndicator()
            }
        }

        errorMessage != null -> {
            CenteredState(modifier = modifier) {
                QStateMessage(
                    message = errorMessage,
                    icon = QIcons.Error,
                    actionText = "Reintentar",
                    onAction = onRetry,
                )
            }
        }

        isEmpty -> {
            CenteredState(modifier = modifier) {
                QStateMessage(
                    message = emptyMessage,
                    actionText = emptyActionText,
                    onAction = onEmptyAction,
                )
            }
        }

        else -> {
            // Misma separación que el template, para que las páginas no pongan la suya.
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(QSpacing.l),
                content = content,
            )
        }
    }
}

/** Centra el estado en el espacio libre. Dentro de un scroll se queda arriba, con aire. */
@Composable
private fun CenteredState(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = QSpacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun PreviewState(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isEmpty: Boolean = false,
) {
    QarinoTheme {
        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = {},
            isEmpty = isEmpty,
            emptyMessage = "Aún no tienes solicitudes.",
            emptyActionText = "Ver catálogo",
            onEmptyAction = {},
        ) {
            QCard(modifier = Modifier.fillMaxWidth()) { QText(text = "Contenido cargado 1") }
            QCard(modifier = Modifier.fillMaxWidth()) { QText(text = "Contenido cargado 2") }
        }
    }
}

@Preview(name = "Cargando", showBackground = true, heightDp = 300)
@Composable
private fun QStateContentLoadingPreview() {
    PreviewState(isLoading = true)
}

@Preview(name = "Error", showBackground = true, heightDp = 300)
@Composable
private fun QStateContentErrorPreview() {
    PreviewState(errorMessage = "No pudimos conectarnos. Revisa tu internet.")
}

@Preview(name = "Vacío", showBackground = true, heightDp = 300)
@Composable
private fun QStateContentEmptyPreview() {
    PreviewState(isEmpty = true)
}

@Preview(name = "Con contenido", showBackground = true, heightDp = 300)
@Composable
private fun QStateContentPreview() {
    PreviewState()
}
