package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Switch con colores del tema (encendido = onAction).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: ToggleSwitch.
 */
@Composable
fun QSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = QTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = colors.onAction,
                checkedBorderColor = colors.onAction,
                uncheckedThumbColor = colors.fieldBorder,
                uncheckedTrackColor = colors.surfaceMuted,
                uncheckedBorderColor = colors.fieldBorder,
            ),
    )
}

@Preview(showBackground = true, name = "QSwitch")
@Composable
private fun QSwitchPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QSwitch(checked = true, onCheckedChange = {})
            QSwitch(checked = false, onCheckedChange = {})
            QSwitch(checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}