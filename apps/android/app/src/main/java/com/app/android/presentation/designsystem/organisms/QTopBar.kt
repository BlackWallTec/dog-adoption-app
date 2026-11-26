// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Top bar de marca. Fondo brand (#FCF0C8) de orilla a orilla incluyendo status bar (statusBarsPadding
 * adentro), círculo brandStrong (#F6D675) de QSize.topBarBlob recortado en la esquina superior
 * derecha. Fila: [QIconButton(QIcons.Back) si onBack != null] + QIcon(QIcons.Paw, QSize.topBarLogo) +
 * QText(title, TopBarTitle) + espacio + QIconButton(QIcons.Notifications, badgeCount). Debajo subtitle
 * (Body, textSecondary, máx 2 líneas). Padding horizontal QSpacing.screenHorizontal, abajo QSpacing.l.
 * Esquinas RECTAS y sombra hacia abajo (QElevation.bar, color shadow).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIconButton, QIcon, QText.
 * Reemplaza a: AppTopBar, TopAppBar.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    QPlaceholder("QTopBar", modifier)
}
