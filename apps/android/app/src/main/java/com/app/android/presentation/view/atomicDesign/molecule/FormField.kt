package com.app.android.presentation.view.atomicDesign.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.view.atomicDesign.atoms.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atoms.SupportingText
import com.app.android.presentation.view.ui.theme.MyApplicationTheme

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String = " ",
) {
    Column(modifier) {
        FloatingLabelTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
        )
        if (supportingText != null) {
            SupportingText(text = supportingText)
        }
        if (isError) {
            ErrorMessage(text = errorText)
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun FormFieldPreview() {
    MyApplicationTheme {
        Column {
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
        }
    }
}

