package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val PILL_WIDTH = 56.dp
private val PILL_HEIGHT = 30.dp

/**
 * Elemento de la nav bar: ícono 24.dp + etiqueta (Caption). Seleccionado: pastilla action (≈56x30.dp)
 * detrás del ícono, ícono y texto en onActionStrong, texto resaltado. No seleccionado: textSecondary.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIcon, QText.
 * Reemplaza a: ButtonNavigation.
 */
@Composable
fun QNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QTheme.colors
    val contentColor = if (selected) colors.onActionStrong else colors.textSecondary
    Column(
        modifier =
            modifier
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(vertical = QSpacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = PILL_WIDTH, height = PILL_HEIGHT)
                    .clip(QShapes.pill)
                    .background(if (selected) colors.action else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            // El ícono es decorativo: la etiqueta de abajo ya dice a dónde lleva
            QIcon(icon = icon, contentDescription = null, tint = contentColor)
        }
        Spacer(modifier = Modifier.height(QSpacing.xs))
        QText(
            text = label,
            // QText no tiene parámetro de peso: el seleccionado usa Label (Medium) en lugar de Caption (Normal)
            style = if (selected) QTextStyle.Label else QTextStyle.Caption,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QNavItem - seleccionado y no seleccionado")
@Composable
private fun QNavItemPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QNavItem(label = "Catálogo", icon = QIcons.Paw, selected = true, onClick = {})
            QNavItem(label = "Ayudar", icon = QIcons.Help, selected = false, onClick = {})
            QNavItem(label = "Donar", icon = QIcons.Donate, selected = false, onClick = {})
            QNavItem(label = "Solicitudes", icon = QIcons.Requests, selected = false, onClick = {})
            QNavItem(label = "Ajustes", icon = QIcons.Settings, selected = false, onClick = {})
        }
    }
}