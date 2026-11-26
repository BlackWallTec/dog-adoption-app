// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Envuelve el contenido según el estado: isLoading -> QLoadingIndicator centrado; errorMessage != null
 * -> QStateMessage(errorMessage, QIcons.Error, 'Reintentar', onRetry); isEmpty ->
 * QStateMessage(emptyMessage, actionText = emptyActionText, onAction = onEmptyAction); si no ->
 * content().
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QLoadingIndicator, QStateMessage.
 * Reemplaza a: EstadoCargando, EstadoError.
 * TODO(P4): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QStateContent", modifier, content)
}
