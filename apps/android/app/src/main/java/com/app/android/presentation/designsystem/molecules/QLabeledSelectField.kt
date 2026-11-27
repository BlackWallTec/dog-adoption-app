package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QSelectField
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Etiqueta (Label) + QSelectField (+ error). Las opciones del menú desplegable pasan directo al QSelectField.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText, QSelectField.
 * Reemplaza a: FilterDropdown.
 */
@Composable
fun QLabeledSelectField(
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar",
    errorText: String? = null,
    enabled: Boolean = true,
    expanded: Boolean = false,
    onDismissRequest: () -> Unit = {},
    options: List<String> = emptyList(),
    onOptionSelected: (Int) -> Unit = {},
) {
    Column(modifier = modifier) {
        QText(text = label, style = QTextStyle.Label)
        Spacer(modifier = Modifier.height(QSpacing.s))
        QSelectField(
            value = value,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder,
            enabled = enabled,
            isError = errorText != null,
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            options = options,
            onOptionSelected = onOptionSelected,
        )
        if (errorText != null) {
            Spacer(modifier = Modifier.height(QSpacing.xs))
            QText(text = errorText, style = QTextStyle.Caption, color = QTheme.colors.error)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QLabeledSelectField - estados")
@Composable
private fun QLabeledSelectFieldPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QLabeledSelectField(label = "Desde", value = null, onClick = {})
            QLabeledSelectField(label = "Desde", value = "15 nov. 2026", onClick = {})
            QLabeledSelectField(label = "Hasta", value = null, onClick = {}, errorText = "Elige una fecha posterior a la inicial.")
            QLabeledSelectField(label = "Hora", value = null, onClick = {}, placeholder = "Elige una hora", enabled = false)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, heightDp = 320, name = "QLabeledSelectField - menú abierto")
@Composable
private fun QLabeledSelectFieldMenuPreview() {
    QarinoTheme {
        QLabeledSelectField(
            label = "Régimen fiscal",
            value = null,
            onClick = {},
            placeholder = "Selecciona tu régimen",
            expanded = true,
            options = listOf("605 · Sueldos y Salarios", "612 · Actividades Empresariales", "626 · Régimen Simplificado de Confianza"),
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
        )
    }
}
