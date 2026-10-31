package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atomic.FloatingLabelTextField
import com.app.android.presentation.view.atomicDesign.atomic.SupportingText
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun FormField(
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
) {
    Column(modifier) {
        FloatingLabelTextField(
            enabled = enabled,
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = isError,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        if (isError && errorText != null) {
                            error(errorText)
                        }
                    },
        )
        if (supportingText != null) {
            SupportingText(text = supportingText)
        }
        if (isError && errorText != null) {
            ErrorMessage(text = errorText)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormFieldPreview() {
    AndroidTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            FormField(
                value = "correo@correo.com",
                onValueChange = {},
                label = "Correo electrónico",
                supportingText = "Usaremos este correo para tu solicitud.",
            )
            FormField(
                value = " ",
                onValueChange = {},
                label = "Correo electrónico",
            )
            FormField(
                value = "Correo 1",
                onValueChange = {},
                label = "Correo electrónico",
                isError = true,
                errorText = "Completa los campos pendientes.",
            )
            FormField(
                enabled = false,
                value = "Desactivado",
                onValueChange = {},
                label = "Correo electrónico",
                isError = false,
                errorText = "Completa los campos pendientes.",
            )
        }
    }
}
