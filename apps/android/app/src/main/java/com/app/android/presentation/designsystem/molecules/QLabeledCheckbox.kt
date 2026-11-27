package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCheckbox
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * QCheckbox + texto; toda la fila es presionable (área táctil mínima de 48dp).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QCheckbox, QText.
 * Reemplaza a: LabeledCheckBox.
 */
@Composable
fun QLabeledCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.buttonHeight)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ),
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // También recibe el callback: así tocar justo la casilla funciona igual que tocar la fila
        QCheckbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        QText(
            text = text,
            color = if (enabled) QTheme.colors.textPrimary else QTheme.colors.disabledContent,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, name = "QLabeledCheckbox - estados")
@Composable
private fun QLabeledCheckboxPreview() {
    QarinoTheme {
        var accepted by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QLabeledCheckbox(
                text = "Acepto el aviso de privacidad y los compromisos de adopción",
                checked = accepted,
                onCheckedChange = { accepted = it },
                modifier = Modifier.fillMaxWidth(),
            )
            QLabeledCheckbox(text = "Marcado", checked = true, onCheckedChange = {})
            QLabeledCheckbox(text = "Deshabilitado sin marcar", checked = false, onCheckedChange = {}, enabled = false)
            QLabeledCheckbox(text = "Deshabilitado marcado", checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}