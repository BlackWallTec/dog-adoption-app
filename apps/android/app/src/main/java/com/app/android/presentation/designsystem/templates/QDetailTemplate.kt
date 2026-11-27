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
 * Pantallas a las que se llega desde otra pantalla (Detalle del perro, Mis programas, Mis donativos…):
 * QScreenScaffold + QTopBar CON flecha (onBack) + QNavBar (si showNavBar) con la pestaña de la
 * sección.
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QScreenScaffold, QTopBar, QNavBar.
 */
@Composable
fun QDetailTemplate(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    selectedTab: QTab,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    showNavBar: Boolean = true,
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
                onBack = onBack,
                onNotificationsClick = onNotificationsClick,
                notificationCount = notificationCount,
            )
        },
        bottomBar = {
            if (showNavBar) {
                QNavBar(
                    selected = selectedTab,
                    onSelect = onTabSelected,
                    containerColor = navBarColor,
                )
            }
        },
        stickyAction = stickyAction,
        background = background,
        scrollable = scrollable,
        content = content,
    )
}

@Composable
private fun PreviewContent() {
    repeat(4) { index ->
        QCard(modifier = Modifier.fillMaxWidth()) {
            QText(text = "Sección de ejemplo ${index + 1}", style = QTextStyle.Title)
            QText(text = "Contenido falso para revisar márgenes y fondo.")
        }
    }
}

@Preview(name = "Detalle del perro (blanco + botón fijo)", showBackground = true, heightDp = 800)
@Composable
private fun QDetailTemplatePlainPreview() {
    QarinoTheme {
        QDetailTemplate(
            title = "Señor Salchicha",
            subtitle = "Conoce a tu posible compañero.",
            onBack = {},
            selectedTab = QTab.Catalogo,
            onTabSelected = {},
            background = QBackground.Plain,
            stickyAction = { QButton(text = "¡Quiero Adoptar!", onClick = {}, fillMaxWidth = true) },
        ) {
            PreviewContent()
        }
    }
}

@Preview(name = "Detalle con nav bar (arena)", showBackground = true, heightDp = 800)
@Composable
private fun QDetailTemplatePreview() {
    QarinoTheme {
        QDetailTemplate(
            title = "Mis Programas",
            subtitle = "Estas son tus próximas jornadas agendadas.",
            onBack = {},
            selectedTab = QTab.Ayudar,
            onTabSelected = {},
        ) {
            PreviewContent()
        }
    }
}

@Preview(name = "Sin nav bar", showBackground = true, heightDp = 800)
@Composable
private fun QDetailTemplateNoNavBarPreview() {
    QarinoTheme {
        QDetailTemplate(
            title = "Solicitud enviada",
            subtitle = null,
            onBack = {},
            selectedTab = QTab.Solicitudes,
            onTabSelected = {},
            showNavBar = false,
        ) {
            PreviewContent()
        }
    }
}
