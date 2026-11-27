package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QFilterChip
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Fila (FlowRow) de QFilterChip. Para selección única el que llama pasa un Set de 1 elemento.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QFilterChip.
 * Reemplaza a: FilterSingleChoiceRow, FilterMultiChoiceFlow, AppOptionPicker, LabeledOptionPicker.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QFilterChipGroup(
    options: List<String>,
    selectedIndices: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        options.forEachIndexed { index, option ->
            QFilterChip(
                text = option,
                selected = index in selectedIndices,
                onClick = { onToggle(index) },
                enabled = enabled,
            )
        }
    }
}

@Preview(showBackground = true, name = "QFilterChipGroup - múltiple, única y deshabilitado")
@Composable
private fun QFilterChipGroupPreview() {
    QarinoTheme {
        val sizes = listOf("Chico", "Mediano", "Grande", "Extra grande")
        val sexes = listOf("Macho", "Hembra")
        var selectedSizes by remember { mutableStateOf(setOf(0, 2)) }
        var selectedSex by remember { mutableStateOf(setOf(0)) }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QText(text = "Tamaño (selección múltiple)", style = QTextStyle.Label)
            QFilterChipGroup(
                options = sizes,
                selectedIndices = selectedSizes,
                onToggle = { index ->
                    selectedSizes = if (index in selectedSizes) selectedSizes - index else selectedSizes + index
                },
            )
            QText(text = "Sexo (selección única: un Set de 1 elemento)", style = QTextStyle.Label)
            QFilterChipGroup(
                options = sexes,
                selectedIndices = selectedSex,
                onToggle = { index -> selectedSex = setOf(index) },
            )
            QText(text = "Deshabilitado", style = QTextStyle.Label)
            QFilterChipGroup(options = sizes, selectedIndices = setOf(1), onToggle = {}, enabled = false)
        }
    }
}