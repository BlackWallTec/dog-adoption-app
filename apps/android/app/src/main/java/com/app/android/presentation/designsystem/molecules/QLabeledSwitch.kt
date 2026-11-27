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
import com.app.android.presentation.designsystem.atoms.QSwitch
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Texto (+ supportingText) a la izquierda y QSwitch a la derecha; toda la fila es presionable.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QSwitch, QText.
 */
@Composable
fun QLabeledSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .heightIn(min = QSize.buttonHeight)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
        horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            QText(text = text, color = if (enabled) QTheme.colors.textPrimary else QTheme.colors.disabledContent)
            if (supportingText != null) {
                QText(text = supportingText, style = QTextStyle.Caption)
            }
        }
        // También recibe el callback: así tocar justo el switch funciona igual que tocar la fila
        QSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Preview(showBackground = true, name = "QLabeledSwitch - estados")
@Composable
private fun QLabeledSwitchPreview() {
    QarinoTheme {
        var notifications by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QLabeledSwitch(
                text = "Notificaciones",
                checked = notifications,
                onCheckedChange = { notifications = it },
                supportingText = "Te avisamos cuando cambie el estado de tu solicitud.",
                modifier = Modifier.fillMaxWidth(),
            )
            QLabeledSwitch(text = "Sin texto de apoyo", checked = false, onCheckedChange = {}, modifier = Modifier.fillMaxWidth())
            QLabeledSwitch(
                text = "Deshabilitado",
                checked = true,
                onCheckedChange = {},
                supportingText = "No se puede cambiar por ahora.",
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}