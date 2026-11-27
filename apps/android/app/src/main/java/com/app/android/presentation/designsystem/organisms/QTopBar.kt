// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QElevation
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

// Centro del círculo (fracciones de topBarBlob): pegado a la esquina superior derecha, como en el mockup.
// Si el círculo debe quedar más a la izquierda, sube BLOB_CENTER_X_FACTOR (p. ej. 0.5f).
private const val BLOB_CENTER_X_FACTOR = 0.05f
private const val BLOB_CENTER_Y_FACTOR = 0.1f

/**
 * Top bar de marca. Fondo brand de orilla a orilla (la status bar entra por statusBarsPadding), círculo
 * brandStrong recortado en la esquina superior derecha, sombra hacia abajo (QElevation.bar).
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIconButton, QIcon, QText.
 * Reemplaza a: AppTopBar, TopAppBar.
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
    val colors = QTheme.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = QElevation.bar, ambientColor = colors.shadow, spotColor = colors.shadow)
                .background(colors.brand)
                .clipToBounds()
                .drawBehind {
                    val blob = QSize.topBarBlob.toPx()
                    drawCircle(
                        color = colors.brandStrong,
                        radius = blob / 2f,
                        center = Offset(size.width - blob * BLOB_CENTER_X_FACTOR, blob * BLOB_CENTER_Y_FACTOR),
                        // Radio = mitad del diámetro topBarBlob (200.dp → 100.dp).
                    )
                }.statusBarsPadding()
                .padding(start = QSpacing.screenHorizontal, end = QSpacing.screenHorizontal, bottom = QSpacing.l),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                QIconButton(icon = QIcons.Back, contentDescription = "Regresar", onClick = onBack)
            }
            QIcon(icon = QIcons.Paw, contentDescription = null, size = QSize.topBarLogo)
            Spacer(modifier = Modifier.width(QSpacing.s))
            QText(
                text = title,
                style = QTextStyle.TopBarTitle,
                maxLines = 1,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            QIconButton(
                icon = QIcons.Notifications,
                contentDescription = "Notificaciones",
                onClick = onNotificationsClick,
                badgeCount = notificationCount,
            )
        }
        if (subtitle != null) {
            QText(
                text = subtitle,
                style = QTextStyle.Body,
                color = colors.textSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = QSpacing.xs),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QTopBar - estados")
@Composable
private fun QTopBarPreview() {
    QarinoTheme {
        Column {
            QTopBar(title = "Catálogo", subtitle = "Encuentra tu nuevo mejor amigo.")
            Spacer(modifier = Modifier.height(QSpacing.s))
            QTopBar(
                title = "Señor Salchicha",
                subtitle = "Conoce a tu posible compañero.",
                onBack = {},
                notificationCount = 3,
            )
            Spacer(modifier = Modifier.height(QSpacing.s))
            QTopBar(title = "Mis solicitudes", notificationCount = 120)
        }
    }
}
