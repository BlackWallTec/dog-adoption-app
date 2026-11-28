package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QRadioButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * QRadioButton + texto; toda la fila es presionable (área táctil mínima de 48dp).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QRadioButton, QText.
 */
@Composable
fun QLabeledRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.buttonHeight)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // onClick = null: la fila completa maneja el toque, así no se anuncia dos veces
        QRadioButton(selected = selected, onClick = null, enabled = enabled)
        QText(
            text = text,
            color = if (enabled) QTheme.colors.textPrimary else QTheme.colors.disabledContent,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Preview(showBackground = true, name = "QLabeledRadioButton - grupo")
@Composable
private fun QLabeledRadioButtonPreview() {
    QarinoTheme {
        val options = listOf("Casa con jardín", "Departamento", "Casa sin jardín")
        var selectedIndex by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
        ) {
            options.forEachIndexed { index, option ->
                QLabeledRadioButton(
                    text = option,
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            QLabeledRadioButton(text = "Deshabilitado", selected = false, onClick = {}, enabled = false)
            QLabeledRadioButton(text = "Deshabilitado seleccionado", selected = true, onClick = {}, enabled = false)
        }
    }
}