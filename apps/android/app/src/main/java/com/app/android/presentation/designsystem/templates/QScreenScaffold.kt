package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QNavBar
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.organisms.QTopBar
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

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
    val screenColor = background.toColor()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(screenColor)
                // Con el teclado abierto, el botón fijo y la nav bar quedan encima de él.
                .imePadding(),
    ) {
        // zIndex: la sombra de la top bar se dibuja encima del contenido.
        Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
            topBar()
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                    ).padding(horizontal = QSpacing.screenHorizontal, vertical = QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
            content = content,
        )

        // Botón fijo + nav bar. El espacio de la barra de gestos va aquí para que también lo
        // respete el botón fijo cuando la pantalla no tiene nav bar (pasos del formulario).
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .background(screenColor)
                    .navigationBarsPadding(),
        ) {
            if (stickyAction != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = QSpacing.screenHorizontal, vertical = QSpacing.m),
                ) {
                    stickyAction()
                }
            }
            bottomBar()
        }
    }
}

@Composable
@ReadOnlyComposable
private fun QBackground.toColor(): Color =
    when (this) {
        QBackground.Default -> QTheme.colors.background
        QBackground.Plain -> QTheme.colors.backgroundPlain
    }

@Composable
private fun PreviewContent(count: Int) {
    repeat(count) { index ->
        QCard(modifier = Modifier.fillMaxWidth()) {
            QText(text = "Tarjeta de ejemplo ${index + 1}", style = QTextStyle.Title)
            QText(text = "Contenido falso para revisar márgenes y fondo.")
        }
    }
}

@Preview(name = "Fondo arena + nav bar", showBackground = true, heightDp = 800)
@Composable
private fun QScreenScaffoldPreview() {
    QarinoTheme {
        QScreenScaffold(
            topBar = { QTopBar(title = "Donar dinero", subtitle = "Subtítulo de ejemplo.") },
            bottomBar = { QNavBar(selected = QTab.Donar, onSelect = {}) },
        ) {
            PreviewContent(count = 3)
        }
    }
}

@Preview(name = "Fondo blanco + botón fijo + nav bar", showBackground = true, heightDp = 800)
@Composable
private fun QScreenScaffoldPlainStickyPreview() {
    QarinoTheme {
        QScreenScaffold(
            topBar = {
                QTopBar(title = "Señor Salchicha", subtitle = "Conoce a tu posible compañero.", onBack = {})
            },
            bottomBar = {
                QNavBar(
                    selected = QTab.Catalogo,
                    onSelect = {},
                    containerColor = QTheme.colors.backgroundPlain,
                )
            },
            stickyAction = { QButton(text = "¡Quiero Adoptar!", onClick = {}, fillMaxWidth = true) },
            background = QBackground.Plain,
        ) {
            PreviewContent(count = 6)
        }
    }
}

@Preview(name = "Sin scroll y sin nav bar", showBackground = true, heightDp = 800)
@Composable
private fun QScreenScaffoldNoScrollPreview() {
    QarinoTheme {
        QScreenScaffold(
            topBar = { QTopBar(title = "Iniciar sesión") },
            scrollable = false,
        ) {
            PreviewContent(count = 2)
        }
    }
}
