// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.designsystem.organisms.QTab

/**
 * Pantallas a las que se llega desde la nav bar (Catálogo, Programas, Donar dinero, Mis solicitudes,
 * Ajustes): QScreenScaffold + QTopBar SIN flecha + QNavBar(selectedTab).
 *
 * Dueño: P4 (Templates).
 * Usa (obligatorio): QScreenScaffold, QTopBar, QNavBar.
 * TODO(P4): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QMainTabTemplate", modifier, content)
}
