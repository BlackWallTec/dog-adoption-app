package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QTypography
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Campo de texto: fondo surface, borde 1.dp fieldBorder (error = error), radio QRadius.small, alto
 * mínimo QSize.fieldHeight. leadingIcon en onAction. isPassword oculta el texto y muestra trailing de
 * ver/ocultar. readOnly muestra el valor sin dejar editarlo (se puede seleccionar y copiar).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: FloatingLabelTextField, FormField, WritableCard.
 */
@Composable
fun QTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
) {
    val colors = QTheme.colors
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val leading: (@Composable () -> Unit)? =
        if (leadingIcon != null) {
            { QIcon(icon = leadingIcon, contentDescription = null, tint = colors.onAction) }
        } else {
            null
        }
    val trailing: (@Composable () -> Unit)? =
        when {
            isPassword -> {
                {
                    QIconButton(
                        icon = if (passwordVisible) QIcons.VisibilityOff else QIcons.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        onClick = { passwordVisible = !passwordVisible },
                        tint = colors.textSecondary,
                    )
                }
            }
            trailingIcon != null && onTrailingIconClick != null -> {
                {
                    QIconButton(
                        icon = trailingIcon,
                        contentDescription = placeholder,
                        onClick = onTrailingIconClick,
                        tint = colors.textSecondary,
                    )
                }
            }
            trailingIcon != null -> {
                { QIcon(icon = trailingIcon, contentDescription = null, tint = colors.textSecondary) }
            }
            else -> null
        }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = QSize.fieldHeight),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = QTypography.of(QTextStyle.Body),
        placeholder = {
            QText(text = placeholder, color = colors.textSecondary, maxLines = if (singleLine) 1 else Int.MAX_VALUE)
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        isError = isError,
        visualTransformation =
            if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        minLines = if (singleLine) 1 else minLines,
        shape = QShapes.small,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.disabledContent,
                errorTextColor = colors.textPrimary,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                disabledContainerColor = colors.surfaceMuted,
                errorContainerColor = colors.surface,
                cursorColor = colors.onAction,
                errorCursorColor = colors.error,
                focusedBorderColor = colors.onAction,
                unfocusedBorderColor = colors.fieldBorder,
                disabledBorderColor = colors.border,
                errorBorderColor = colors.error,
            ),
    )
}

@Preview(showBackground = true, name = "QTextField - estados")
@Composable
private fun QTextFieldPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QTextField(
                value = "",
                onValueChange = {},
                placeholder = "$0 MXN",
                leadingIcon = QIcons.Money,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "Folio 12345",
                onValueChange = {},
                placeholder = "Folio o referencia",
                leadingIcon = QIcons.Hash,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "secreto",
                onValueChange = {},
                placeholder = "Contraseña",
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "correo-mal",
                onValueChange = {},
                placeholder = "Correo",
                isError = true,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "",
                onValueChange = {},
                placeholder = "Cuéntanos sobre tu hogar",
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "No editable",
                onValueChange = {},
                placeholder = "",
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            QTextField(
                value = "Solo lectura (readOnly)",
                onValueChange = {},
                placeholder = "",
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}