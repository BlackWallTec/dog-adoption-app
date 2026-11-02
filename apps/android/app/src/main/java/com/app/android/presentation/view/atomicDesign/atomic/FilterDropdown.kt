package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorSecondary = Color(0xFF2F6FB5)
private val ColorSurfaceContainer = Color(0xFFF1EBDD)
private val ColorTextMain = Color(0xFF3A3A3A)
private val ColorOutlineVariant = Color(0xFFD2CABB)

// Desplegable de selección única. Las opciones vienen de afuera (por ejemplo, las razas desde Supabase).
// selectedIndex = -1 muestra el placeholder; elegir el placeholder limpia la selección.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Todas las razas",
    contentDescriptionLabel: String = "Raza",
    enabled: Boolean = true,
) {
    val expanded = remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    // getOrNull: si el índice no existe (lista vacía o aún cargando), muestra el placeholder
    val displayText = options.getOrNull(selectedIndex) ?: placeholder

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it && enabled },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "$contentDescriptionLabel: $displayText"
                    },
        ) {
            Row(
                modifier =
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(shape)
                        .background(ColorSurfaceContainer)
                        .border(width = 1.dp, color = ColorOutlineVariant, shape = shape)
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayText,
                    color = ColorTextMain,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = ColorSecondary,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                DropdownMenuItem(
                    text = { Text(placeholder) },
                    onClick = {
                        onSelected(-1)
                        expanded.value = false
                    },
                )
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(index)
                            expanded.value = false
                        },
                    )
                }
            }
        }
    }
}

// Los nombres de raza de los previews son solo datos de ejemplo.
@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterDropdownPreviewClosed() {
    FilterDropdown(
        options = listOf("Mestizo", "Labrador", "Pastor alemán"),
        selectedIndex = -1,
        onSelected = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterDropdownPreviewSelected() {
    FilterDropdown(
        options = listOf("Mestizo", "Labrador", "Pastor alemán"),
        selectedIndex = 1,
        onSelected = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterDropdownPreviewLoading() {
    // Sin razas todavía (por ejemplo, mientras carga Supabase): no truena y deshabilita el campo
    FilterDropdown(
        options = emptyList(),
        selectedIndex = 3,
        onSelected = {},
        enabled = false,
    )
}