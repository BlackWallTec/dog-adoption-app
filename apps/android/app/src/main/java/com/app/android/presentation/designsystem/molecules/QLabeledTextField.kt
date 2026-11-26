// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Etiqueta (BodyStrong) + QTextField + texto de ayuda (Caption) o de error (Caption en error).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText, QTextField.
 * Reemplaza a: LabeledWritableCard, FormField (con label).
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QLabeledTextField", modifier)
}
