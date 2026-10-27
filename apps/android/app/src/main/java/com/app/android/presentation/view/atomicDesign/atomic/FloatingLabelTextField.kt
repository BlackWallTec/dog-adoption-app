package com.app.android.presentation.view.atomicDesign.molecules

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
import com.app.android.presentation.view.ui.theme.MyApplicationTheme

@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
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
                                Color(0xFF34312B)
                            },
                    )
                },
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFFFBF2),
                                unfocusedContainerColor = Color(0xFFFFFFFF),
                                focusedBorderColor = Color(0xFF34312B),
                                unfocusedBorderColor = Color(0xFF34312B),
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorContainerColor = Color(0xFFFFFBF2),
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
    MyApplicationTheme {
        FloatingLabelTextField(
            value = "Correo electrónico",
            onValueChange = {},
            label = "Correo electrónico",
        )
    }
}