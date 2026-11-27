package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Checkbox con colores del tema (marcado = onAction).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: CheckBox.
 */
@Composable
fun QCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = QTheme.colors
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors =
            CheckboxDefaults.colors(
                checkedColor = colors.onAction,
                uncheckedColor = colors.fieldBorder,
                checkmarkColor = colors.surface,
                disabledCheckedColor = colors.border,
                disabledUncheckedColor = colors.border,
            ),
    )
}

@Preview(showBackground = true, name = "QCheckbox")
@Composable
private fun QCheckboxPreview() {
    QarinoTheme {
        Row {
            QCheckbox(checked = true, onCheckedChange = {})
            QCheckbox(checked = false, onCheckedChange = {})
            QCheckbox(checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}