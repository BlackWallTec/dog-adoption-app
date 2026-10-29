package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface

// MOLÉCULA: pregunta en negrita + botones de opciones
@Composable
fun LabeledOptionPicker(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = InverseSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        AppOptionPicker(
            options = options,
            selectedIndex = selectedIndex,
            onOptionSelected = onOptionSelected,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}