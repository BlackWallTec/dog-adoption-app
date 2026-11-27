package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Chip de filtro presionable. Seleccionado = fondo action + check + texto onActionStrong. No
 * seleccionado = fondo surface + borde border + texto textPrimary.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: FilterButton.
 */
@Composable
fun QFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = QTheme.colors
    val background = if (selected) colors.action else colors.surface
    val content =
        when {
            !enabled -> colors.disabledContent
            selected -> colors.onActionStrong
            else -> colors.textPrimary
        }
    val borderModifier =
        if (selected) Modifier else Modifier.border(width = QSize.border, color = colors.border, shape = QShapes.pill)
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.chipHeight)
                .clip(QShapes.pill)
                .background(background)
                .then(borderModifier)
                .toggleable(
                    value = selected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                ).padding(horizontal = QSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QSpacing.xs),
    ) {
        if (selected) {
            QIcon(icon = QIcons.Check, contentDescription = null, tint = content, size = QSize.iconSmall)
        }
        QText(text = text, style = QTextStyle.Label, color = content, maxLines = 1)
    }
}

@Preview(showBackground = true, name = "QFilterChip")
@Composable
private fun QFilterChipPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QFilterChip(text = "Macho", selected = true, onClick = {})
            QFilterChip(text = "Convive con gatos", selected = false, onClick = {})
            QFilterChip(text = "Deshabilitado", selected = false, onClick = {}, enabled = false)
        }
    }
}