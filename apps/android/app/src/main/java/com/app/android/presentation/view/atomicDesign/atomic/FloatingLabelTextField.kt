package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BasicTextField(
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value.ifEmpty { " " },
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                isError = isError,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = label,
                        color =
                            if (isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.inverseSurface
                            },
                    )
                },
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = Color(0xFFFFFFFF),
                                focusedBorderColor = MaterialTheme.colorScheme.inverseSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.inverseSurface,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                    )
                },
            )
        },
    )
}

@Preview
@Composable
private fun FloatingLabelTextFieldPreview() {
    AndroidTheme {
        FloatingLabelTextField(
            value = "Correo electrónico",
            onValueChange = {},
            label = "Correo electrónico",
            isError = false,
        )
    }
}
