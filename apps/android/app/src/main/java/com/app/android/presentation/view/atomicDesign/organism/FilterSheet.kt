package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.molecule.FilterActionBar
import com.app.android.presentation.view.atomicDesign.molecule.FilterMultiChoiceFlow
import com.app.android.presentation.view.atomicDesign.molecule.FilterSingleChoiceRow
import com.app.android.presentation.view.atomicDesign.atomic.FilterDropdown
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterOptions
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterSelection

private val ColorSurface = Color(0xFFFFFBF2)
private val ColorTextMain = Color(0xFF3A3A3A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheetContent(
    filters: FilterSelection,
    options: FilterOptions,
    onFiltersChange: (FilterSelection) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentEnforcement provides false
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ColorSurface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Encabezado de filtros" },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextMain
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.semantics { contentDescription = "Cerrar filtros" }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar filtros",
                        tint = ColorTextMain,
                        modifier = Modifier.fillMaxWidth(0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tamaño
            Text(
                text = "Tamaño",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorTextMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FilterSingleChoiceRow(
                options = options.tamanos,
                selectedIndex = filters.tamanoIndex,
                onSelected = { index ->
                    onFiltersChange(filters.copy(tamanoIndex = index))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edad
            Text(
                text = "Edad",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorTextMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FilterSingleChoiceRow(
                options = options.edades,
                selectedIndex = filters.edadIndex,
                onSelected = { index ->
                    onFiltersChange(filters.copy(edadIndex = index))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sexo
            Text(
                text = "Sexo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorTextMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FilterSingleChoiceRow(
                options = options.sexos,
                selectedIndex = filters.sexoIndex,
                onSelected = { index ->
                    onFiltersChange(filters.copy(sexoIndex = index))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Raza
            Text(
                text = "Raza",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorTextMain,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FilterDropdown(
                options = options.razas,
                selectedIndex = filters.razaIndex,
                onSelected = { index ->
                    onFiltersChange(filters.copy(razaIndex = index))
                },
                placeholder = "Todas las razas",
                contentDescriptionLabel = "Raza"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Características especiales
            if (options.caracteristicas.isNotEmpty()) {
                Text(
                    text = "Características especiales",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorTextMain,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FilterMultiChoiceFlow(
                    options = options.caracteristicas,
                    selectedIndices = filters.caracteristicas,
                    onToggled = { index ->
                        val newSet = filters.caracteristicas.toMutableSet()
                        if (index in newSet) newSet.remove(index) else newSet.add(index)
                        onFiltersChange(filters.copy(caracteristicas = newSet))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            FilterActionBar(
                onClear = onClear,
                onApply = onApply,
                clearEnabled = !filters.isEmpty,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    applied: FilterSelection,
    options: FilterOptions,
    onApply: (FilterSelection) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean
) {
    if (!isVisible) return

    val draftFilters = remember(applied) { mutableStateOf(applied) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.semantics { contentDescription = "Hoja de filtros" }
    ) {
        FilterSheetContent(
            filters = draftFilters.value,
            options = options,
            onFiltersChange = { draftFilters.value = it },
            onClear = {
                draftFilters.value = FilterSelection()
                onClear()
            },
            onApply = {
                onApply(draftFilters.value)
                onDismiss()
            },
            onClose = onDismiss
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterSheetContentPreviewWithSelections() {
    val state = remember {
        mutableStateOf(
            FilterSelection(
                tamanoIndex = 1,
                edadIndex = 1,
                sexoIndex = 0,
                razaIndex = -1,
                caracteristicas = setOf(0)
            )
        )
    }

    FilterSheetContent(
        filters = state.value,
        options = FilterOptions(
            tamanos = listOf("Chico", "Mediano", "Grande"),
            edades = listOf("Cachorro", "Adulto", "Senior"),
            sexos = listOf("Macho", "Hembra"),
            razas = listOf("Mestizo", "Labrador", "Pastor alemán", "Chihuahua"),
            caracteristicas = listOf("Convive con gatos", "Cuidados especiales")
        ),
        onFiltersChange = { state.value = it },
        onClear = { state.value = FilterSelection() },
        onApply = {},
        onClose = {}
    )
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterSheetContentPreviewEmpty() {
    val state = remember { mutableStateOf(FilterSelection()) }

    FilterSheetContent(
        filters = state.value,
        options = FilterOptions(
            tamanos = listOf("Chico", "Mediano", "Grande"),
            edades = listOf("Cachorro", "Adulto", "Senior"),
            sexos = listOf("Macho", "Hembra"),
            razas = listOf("Mestizo", "Labrador", "Pastor alemán", "Chihuahua"),
            caracteristicas = listOf("Convive con gatos", "Cuidados especiales")
        ),
        onFiltersChange = { state.value = it },
        onClear = { state.value = FilterSelection() },
        onApply = {},
        onClose = {}
    )
}

@Preview(widthDp = 360, heightDp = 640, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun FilterSheetPreviewFullScreen() {
    val isVisible = remember { mutableStateOf(true) }
    val appliedFilters = remember {
        mutableStateOf(
            FilterSelection(
                tamanoIndex = 1,
                edadIndex = 1,
                sexoIndex = 0,
                razaIndex = -1,
                caracteristicas = setOf(0)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        FilterSheet(
            applied = appliedFilters.value,
            options = FilterOptions(
                tamanos = listOf("Chico", "Mediano", "Grande"),
                edades = listOf("Cachorro", "Adulto", "Senior"),
                sexos = listOf("Macho", "Hembra"),
                razas = listOf("Mestizo", "Labrador", "Pastor alemán", "Chihuahua"),
                caracteristicas = listOf("Convive con gatos", "Cuidados especiales")
            ),
            onApply = { appliedFilters.value = it; isVisible.value = false },
            onClear = { appliedFilters.value = FilterSelection() },
            onDismiss = { isVisible.value = false },
            isVisible = isVisible.value
        )
    }
}