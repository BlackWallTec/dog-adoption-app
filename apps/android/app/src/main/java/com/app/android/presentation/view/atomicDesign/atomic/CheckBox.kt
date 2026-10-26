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
import androidx.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
fun CheckBox() {
    var checked by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { isChecked ->
                checked = isChecked
            },
        )
        Text(text = "He leído y acepto los requisitos y las responsabilidades de una adopción responsable.")
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview
@Composable
fun CheckBoxPreview() {
    CheckBox()
}
