package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("ktlint:standard:function-naming")
@Composable
fun ToggleSwitch(
    on: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = on,
        onCheckedChange = onChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = Color(0xFF2F6FB5), //activado
            uncheckedTrackColor = Color(0xFFE6E0E9), //desactivado
        ),
        thumbContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (on) Color.White else Color(0xFF79747E),
                        shape = CircleShape,
                    ),
            )
        },
    )
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Off")
@Composable
fun ToggleSwitchOffPreview() {
    ToggleSwitch(on = false, onChange = {})
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "On")
@Composable
fun ToggleSwitchOnPreview() {
    ToggleSwitch(on = true, onChange = {})
}