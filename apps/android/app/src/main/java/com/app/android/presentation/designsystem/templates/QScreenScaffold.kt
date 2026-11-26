// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

enum class QBackground { Default, Plain }

/**
 * Base de TODAS las pantallas: topBar arriba (pegada), contenido con padding horizontal
 * QSpacing.screenHorizontal y vertical QSpacing.l, separación entre hijos QSpacing.l, scroll vertical
 * si scrollable, stickyAction (botón fijo) encima de bottomBar con padding QSpacing.screenHorizontal,
 * bottomBar abajo (pegada). Fondo: Default = colors.background (#F3EEDF), Plain =
 * colors.backgroundPlain (#FFFFFF) en TODA la pantalla incluida la nav bar. Si scrollable=false el
 * contenido debe poder usar LazyColumn/LazyVerticalGrid (no anidar scroll).
 *
 * Dueño: P4 (Templates).
 * TODO(P4): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QScreenScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    stickyAction: (@Composable () -> Unit)? = null,
    background: QBackground = QBackground.Default,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    QPlaceholder("QScreenScaffold", modifier) {
        topBar()
        content()
        stickyAction?.invoke()
        bottomBar()
    }
}
