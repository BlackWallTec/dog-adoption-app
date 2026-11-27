// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.molecules.QNavItem
import com.app.android.presentation.designsystem.theme.QElevation
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

enum class QTab(
    val label: String,
) {
    Catalogo("Catálogo"),
    Ayudar("Ayudar"),
    Donar("Donar"),
    Solicitudes("Solicitudes"),
    Ajustes("Ajustes"),
}

/**
 * Nav bar de abajo con las 5 pestañas de QTab en ese orden (Catálogo, Ayudar, Donar, Solicitudes,
 * Ajustes) usando QNavItem. Fondo containerColor (Unspecified = background #F3EEDF), sombra hacia
 * arriba (QElevation.bar), navigationBarsPadding.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QNavItem.
 * Reemplaza a: NavBar.
 */
@Composable
fun QNavBar(
    selected: QTab,
    onSelect: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
) {
    val colors = QTheme.colors
    val background = if (containerColor != Color.Unspecified) containerColor else colors.background
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = QElevation.bar, ambientColor = colors.shadow, spotColor = colors.shadow)
                .background(background)
                .navigationBarsPadding()
                .heightIn(min = QSize.navBarHeight)
                .padding(horizontal = QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QTab.entries.forEach { tab ->
            QNavItem(
                label = tab.label,
                icon = tab.icon(),
                selected = tab == selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// Ícono decorativo de cada pestaña; la etiqueta ya dice a dónde lleva.
private fun QTab.icon(): ImageVector =
    when (this) {
        QTab.Catalogo -> QIcons.Paw
        QTab.Ayudar -> QIcons.Help
        QTab.Donar -> QIcons.Donate
        QTab.Solicitudes -> QIcons.Requests
        QTab.Ajustes -> QIcons.Settings
    }

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QNavBar - estados")
@Composable
private fun QNavBarPreview() {
    QarinoTheme {
        val selected = remember { mutableStateOf(QTab.Catalogo) }
        QNavBar(selected = selected.value, onSelect = { selected.value = it })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "QNavBar - Solicitudes con fondo blanco")
@Composable
private fun QNavBarSolicitudesPreview() {
    QarinoTheme {
        QNavBar(
            selected = QTab.Solicitudes,
            onSelect = {},
            containerColor = QTheme.colors.backgroundPlain,
        )
    }
}
