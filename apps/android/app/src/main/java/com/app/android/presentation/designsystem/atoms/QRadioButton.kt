package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Radio con colores del tema (seleccionado = onAction).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: SeleccionUnicaButton.
 */
@Composable
fun QRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = QTheme.colors
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            RadioButtonDefaults.colors(
                selectedColor = colors.onAction,
                unselectedColor = colors.fieldBorder,
                disabledSelectedColor = colors.border,
                disabledUnselectedColor = colors.border,
            ),
    )
}

@Preview(showBackground = true, name = "QRadioButton")
@Composable
private fun QRadioButtonPreview() {
    QarinoTheme {
        Row {
            QRadioButton(selected = true, onClick = {})
            QRadioButton(selected = false, onClick = {})
            QRadioButton(selected = true, onClick = {}, enabled = false)
        }
    }
}