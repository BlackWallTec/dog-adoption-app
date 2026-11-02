package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.FilterButton

@Composable
fun FilterSingleChoiceRow(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            FilterButton(
                text = option,
                selected = index == selectedIndex,
                onClick = {
                    if (index == selectedIndex) {
                        onSelected(-1)
                    } else {
                        onSelected(index)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                role = Role.RadioButton
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterMultiChoiceFlow(
    options: List<String>,
    selectedIndices: Set<Int>,
    onToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            FilterButton(
                text = option,
                selected = index in selectedIndices,
                onClick = {
                    onToggled(index)
                },
                showCheck = true,
                enabled = enabled,
                role = Role.Checkbox
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterSingleChoiceRowPreviewTamano() {
    FilterSingleChoiceRow(
        options = listOf("Chico", "Mediano", "Grande"),
        selectedIndex = 1,
        onSelected = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterSingleChoiceRowPreviewEdad() {
    FilterSingleChoiceRow(
        options = listOf("Cachorro", "Adulto", "Senior"),
        selectedIndex = 1,
        onSelected = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterSingleChoiceRowPreviewSexo() {
    FilterSingleChoiceRow(
        options = listOf("Macho", "Hembra"),
        selectedIndex = 0,
        onSelected = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterMultiChoiceFlowPreviewCaracteristicas() {
    FilterMultiChoiceFlow(
        options = listOf("Convive con gatos", "Cuidados especiales"),
        selectedIndices = setOf(0),
        onToggled = {}
    )
}
