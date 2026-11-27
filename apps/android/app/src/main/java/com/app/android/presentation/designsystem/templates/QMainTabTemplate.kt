package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QNavBar
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.organisms.QTopBar
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Pantallas a las que se llega desde la nav bar (Catálogo, Programas, Donar dinero, Mis solicitudes,
 * Ajustes): QScreenScaffold + QTopBar SIN flecha + QNavBar(selectedTab).
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QScreenScaffold, QTopBar, QNavBar.
 */
@Composable
fun QMainTabTemplate(
    title: String,
    subtitle: String?,
    selectedTab: QTab,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
    background: QBackground = QBackground.Default,
    stickyAction: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Plain = toda la pantalla blanca, incluida la nav bar. Default = el color propio de la nav bar.
    val navBarColor =
        if (background == QBackground.Plain) QTheme.colors.backgroundPlain else Color.Unspecified

    QScreenScaffold(
        modifier = modifier,
        topBar = {
            QTopBar(
                title = title,
                subtitle = subtitle,
                onNotificationsClick = onNotificationsClick,
                notificationCount = notificationCount,
            )
        },
        bottomBar = {
            QNavBar(
                selected = selectedTab,
                onSelect = onTabSelected,
                containerColor = navBarColor,
            )
        },
        stickyAction = stickyAction,
        background = background,
        scrollable = scrollable,
        content = content,
    )
}

@Composable
private fun PreviewContent() {
    QButton(text = "Ver mis donativos", onClick = {}, fillMaxWidth = true)
    repeat(3) { index ->
        QCard(modifier = Modifier.fillMaxWidth()) {
            QText(text = "Tarjeta de ejemplo ${index + 1}", style = QTextStyle.Title)
            QText(text = "Contenido falso para revisar márgenes y fondo.")
        }
    }
}

@Preview(name = "Pestaña principal", showBackground = true, heightDp = 800)
@Composable
private fun QMainTabTemplatePreview() {
    QarinoTheme {
        QMainTabTemplate(
            title = "Donar dinero",
            subtitle = "Realiza una transferencia y adjunta tu comprobante para registrar tu donativo.",
            selectedTab = QTab.Donar,
            onTabSelected = {},
            notificationCount = 2,
        ) {
            PreviewContent()
        }
    }
}

@Preview(name = "Fondo blanco + botón fijo", showBackground = true, heightDp = 800)
@Composable
private fun QMainTabTemplatePlainStickyPreview() {
    QarinoTheme {
        QMainTabTemplate(
            title = "Catálogo",
            subtitle = null,
            selectedTab = QTab.Catalogo,
            onTabSelected = {},
            background = QBackground.Plain,
            stickyAction = { QButton(text = "Aplicar", onClick = {}, fillMaxWidth = true) },
        ) {
            PreviewContent()
        }
    }
}
