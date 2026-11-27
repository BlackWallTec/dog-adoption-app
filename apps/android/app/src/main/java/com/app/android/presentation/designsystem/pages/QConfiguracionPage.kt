package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.molecules.QListItem
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QMainTabTemplate
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Ajustes (pestaña Ajustes): accesos a Mi cuenta, Mis solicitudes, Mis programas y Datos fiscales, agrupados
 * en "Mi perfil" y "Tu cuenta". Solo tiene los renglones cuya pantalla ya existe.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QMainTabTemplate, QSection, QCard, QListItem, QDivider.
 * Reemplaza a: ConfiguracionScreen.
 */
@Composable
fun QConfiguracionPage(
    onMyAccountClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onMyProgramsClick: () -> Unit,
    onFiscalDataClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QMainTabTemplate(
        title = "Configuración",
        subtitle = "Administra tu cuenta y tus preferencias.",
        selectedTab = QTab.Ajustes,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QSection(title = "Mi perfil") {
            QCard(modifier = Modifier.fillMaxWidth()) {
                QListItem(
                    title = "Mi cuenta",
                    onClick = onMyAccountClick,
                    subtitle = "Tus datos de contacto",
                    icon = QIcons.Email,
                )
                QDivider()
                QListItem(title = "Mis solicitudes", onClick = onMyRequestsClick, icon = QIcons.Requests)
                QDivider()
                QListItem(title = "Mis programas", onClick = onMyProgramsClick, icon = QIcons.Help)
            }
        }

        QSection(title = "Tu cuenta") {
            QCard(modifier = Modifier.fillMaxWidth()) {
                QListItem(
                    title = "Datos fiscales",
                    onClick = onFiscalDataClick,
                    subtitle = "Información para tus CFDI",
                    icon = QIcons.Document,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800, name = "QConfiguracionPage")
@Composable
private fun QConfiguracionPagePreview() {
    QarinoTheme {
        QConfiguracionPage(
            onMyAccountClick = {},
            onMyRequestsClick = {},
            onMyProgramsClick = {},
            onFiscalDataClick = {},
            onTabSelected = {},
            notificationCount = 3,
        )
    }
}
