package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Campo que abre un selector (fecha, hora, lista). Mismo estilo visual que el campo de texto pero en
 * forma pill; muestra placeholder si value es null.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: FilterDropdown (la parte visual).
 */
@Composable
fun QSelectField(
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar",
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val colors = QTheme.colors
    val textColor =
        when {
            !enabled -> colors.disabledContent
            value == null -> colors.textSecondary
            else -> colors.textPrimary
        }
    val borderColor =
        when {
            isError -> colors.error
            !enabled -> colors.border
            else -> colors.fieldBorder
        }
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.fieldHeight)
                .clip(QShapes.pill)
                .background(if (enabled) colors.surface else colors.surfaceMuted)
                .border(width = QSize.border, color = borderColor, shape = QShapes.pill)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = QSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        QText(text = value ?: placeholder, color = textColor, maxLines = 1, modifier = Modifier.weight(1f))
        if (trailingIcon != null) {
            QIcon(icon = trailingIcon, contentDescription = null, tint = colors.textSecondary)
        }
    }
}

@Preview(showBackground = true, name = "QSelectField - estados")
@Composable
private fun QSelectFieldPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QSelectField(value = null, onClick = {}, modifier = Modifier.fillMaxWidth())
            QSelectField(value = "Dom. 15 nov. 2026", onClick = {}, modifier = Modifier.fillMaxWidth())
            QSelectField(
                value = "Labrador",
                onClick = {},
                trailingIcon = QIcons.ExpandMore,
                modifier = Modifier.fillMaxWidth(),
            )
            QSelectField(value = null, onClick = {}, isError = true, modifier = Modifier.fillMaxWidth())
            QSelectField(value = null, onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}