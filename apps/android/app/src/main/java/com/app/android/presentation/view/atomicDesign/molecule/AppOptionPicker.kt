package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme

private const val DISABLED_ALPHA = 0.4f

// Para seleccionar entre las opciones (2 o 3)
// Nada mas recibe del ViewModel como se deberia ver
@Composable
fun AppOptionPicker(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    require(options.size in 2..3) { "AppOptionPicker soporta 2 o 3 opciones, recibió ${options.size}" }

    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(48.dp)

    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val containerColor = if (isSelected) colors.secondaryContainer else colors.surface
            val borderColor = if (isSelected) colors.secondaryContainer else colors.outline
            val contentColor = if (isSelected) colors.secondary else colors.onSurface
            val alpha = if (enabled) 1f else DISABLED_ALPHA

            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(shape)
                        .background(containerColor.copy(alpha = containerColor.alpha * alpha))
                        .border(1.dp, borderColor.copy(alpha = borderColor.alpha * alpha), shape)
                        .selectable(
                            selected = isSelected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onOptionSelected(index) },
                        ).padding(horizontal = 4.dp),
            ) {
                Text(
                    text = label,
                    color = contentColor.copy(alpha = alpha),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, name = "App Option Picker")
@Composable
private fun AppOptionPickerPreview() {
    AndroidTheme {
        var threeSelected by remember { mutableIntStateOf(0) }
        var twoSelected by remember { mutableIntStateOf(1) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppOptionPicker(
                options = listOf("Casa", "Departamento", "Otro"),
                selectedIndex = threeSelected,
                onOptionSelected = { threeSelected = it },
            )
            AppOptionPicker(
                options = listOf("Programas", "Mis programas"),
                selectedIndex = twoSelected,
                onOptionSelected = { twoSelected = it },
            )
            AppOptionPicker(
                options = listOf("Casa", "Departamento", "Otro"),
                selectedIndex = 0,
                onOptionSelected = {},
                enabled = false,
            )
        }
    }
}
