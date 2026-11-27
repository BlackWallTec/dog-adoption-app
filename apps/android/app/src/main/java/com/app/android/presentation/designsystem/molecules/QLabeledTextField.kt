package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.atoms.QTextField
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Etiqueta (BodyStrong) + QTextField + texto de ayuda (Caption) o de error (Caption en error).
 * Si hay errorText, el campo se pinta en error y se muestra ese texto en lugar de la ayuda.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText, QTextField.
 * Reemplaza a: LabeledWritableCard, FormField (con label).
 */
@Composable
fun QLabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier = modifier) {
        QText(text = label, style = QTextStyle.BodyStrong)
        Spacer(modifier = Modifier.height(QSpacing.s))
        QTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon,
            enabled = enabled,
            isError = errorText != null,
            singleLine = singleLine,
            minLines = minLines,
            isPassword = isPassword,
            keyboardOptions = keyboardOptions,
        )
        // El error tiene prioridad sobre la ayuda: nunca se muestran los dos a la vez
        val supporting = errorText ?: helperText
        if (supporting != null) {
            Spacer(modifier = Modifier.height(QSpacing.xs))
            QText(
                text = supporting,
                style = QTextStyle.Caption,
                color = if (errorText != null) QTheme.colors.error else QTheme.colors.textSecondary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QLabeledTextField - estados")
@Composable
private fun QLabeledTextFieldPreview() {
    QarinoTheme {
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("secreto123") }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QLabeledTextField(
                label = "Nombre completo",
                value = name,
                onValueChange = { name = it },
                placeholder = "Escribe tu nombre",
            )
            QLabeledTextField(
                label = "Referencia bancaria (opcional)",
                value = "",
                onValueChange = {},
                placeholder = "Folio o referencia de tu transferencia",
                leadingIcon = QIcons.Hash,
                helperText = "Ayuda al refugio a encontrar tu transferencia más rápido.",
            )
            QLabeledTextField(
                label = "Correo",
                value = "correo-invalido",
                onValueChange = {},
                placeholder = "correo@ejemplo.com",
                leadingIcon = QIcons.Email,
                helperText = "Esta ayuda no se ve porque hay error.",
                errorText = "Escribe un correo válido.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            QLabeledTextField(
                label = "Contraseña",
                value = password,
                onValueChange = { password = it },
                placeholder = "Mínimo 8 caracteres",
                leadingIcon = QIcons.Lock,
                isPassword = true,
            )
            QLabeledTextField(
                label = "¿Por qué quieres adoptar?",
                value = "",
                onValueChange = {},
                placeholder = "Cuéntanos",
                singleLine = false,
                minLines = 3,
            )
            QLabeledTextField(
                label = "Campo deshabilitado",
                value = "No se puede editar",
                onValueChange = {},
                placeholder = "",
                enabled = false,
            )
        }
    }
}