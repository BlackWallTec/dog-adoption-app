package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
fun CheckBox(
    checked: Boolean, // ¿está palomeado? (lo decide quien lo usa)
    onCheckedChange: (Boolean) -> Unit, // avisa cuando lo tocan
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(text = text)
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview
@Composable
fun CheckBoxPreview() {
    var checked by remember { mutableStateOf(false) }
    CheckBox(
        checked = checked,
        onCheckedChange = { checked = it },
        text = "He leído y acepto los requisitos y las responsabilidades de una adopción responsable.",
    )
}
