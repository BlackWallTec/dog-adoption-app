// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QFilterChipGroup
import com.app.android.presentation.designsystem.molecules.QLabeledSelectField
import com.app.android.presentation.designsystem.molecules.QSectionHeader
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterOptions
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterSelection

/**
 * Bottom sheet de filtros del catálogo (misma lógica que FilterSheet): Tamaño, Edad y Sexo de selección única
 * (tocar el seleccionado lo deselecciona), Raza con un selector que abre un DropdownMenu y Características
 * especiales de selección múltiple (se omite si no hay opciones). Limpiar y Aplicar filtros al final.
 *
 * El borrador (draft) arranca de applied y se edita mientras la hoja está abierta. Aplicar llama onApply(draft)
 * y luego onDismiss, igual que FilterSheet.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QSectionHeader, QFilterChipGroup, QLabeledSelectField, QButton.
 * Reemplaza a: FilterSheet, FilterSheetContent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QCatalogFilterSheet(
    applied: FilterSelection,
    options: FilterOptions,
    onApply: (FilterSelection) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val draft = remember(applied) { mutableStateOf(applied) }
    val razaMenuOpen = remember { mutableStateOf(false) }
    if (!isVisible) return

    val current = draft.value
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = QShapes.large,
        containerColor = QTheme.colors.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = QSpacing.screenHorizontal)
                    .padding(bottom = QSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QText(
                    text = "Filtrar",
                    style = QTextStyle.Title,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                QIconButton(
                    icon = QIcons.Close,
                    contentDescription = "Cerrar filtros",
                    onClick = onDismiss,
                )
            }

            SingleChoiceSection(
                title = "Tamaño",
                options = options.tamanos,
                selectedIndex = current.tamanoIndex,
                onSelect = { index -> draft.value = draft.value.copy(tamanoIndex = index) },
            )
            SingleChoiceSection(
                title = "Edad",
                options = options.edades,
                selectedIndex = current.edadIndex,
                onSelect = { index -> draft.value = draft.value.copy(edadIndex = index) },
            )
            SingleChoiceSection(
                title = "Sexo",
                options = options.sexos,
                selectedIndex = current.sexoIndex,
                onSelect = { index -> draft.value = draft.value.copy(sexoIndex = index) },
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                QLabeledSelectField(
                    label = "Raza",
                    value = options.razas.getOrNull(current.razaIndex),
                    onClick = { razaMenuOpen.value = true },
                    placeholder = "Todas las razas",
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownMenu(
                    expanded = razaMenuOpen.value,
                    onDismissRequest = { razaMenuOpen.value = false },
                ) {
                    DropdownMenuItem(
                        text = { QText(text = "Todas las razas") },
                        onClick = {
                            draft.value = draft.value.copy(razaIndex = -1)
                            razaMenuOpen.value = false
                        },
                    )
                    options.razas.forEachIndexed { index, raza ->
                        DropdownMenuItem(
                            text = { QText(text = raza) },
                            onClick = {
                                draft.value = draft.value.copy(razaIndex = index)
                                razaMenuOpen.value = false
                            },
                        )
                    }
                }
            }

            if (options.caracteristicas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QSectionHeader(title = "Características especiales")
                    QFilterChipGroup(
                        options = options.caracteristicas,
                        selectedIndices = current.caracteristicas,
                        onToggle = { index ->
                            val updated = draft.value.caracteristicas.toMutableSet()
                            if (!updated.add(index)) updated.remove(index)
                            draft.value = draft.value.copy(caracteristicas = updated)
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
            ) {
                QButton(
                    text = "Limpiar",
                    onClick = {
                        draft.value = FilterSelection()
                        onClear()
                    },
                    enabled = !current.isEmpty,
                    fillMaxWidth = true,
                    modifier = Modifier.weight(1f),
                )
                QButton(
                    text = "Aplicar filtros",
                    onClick = {
                        onApply(draft.value)
                        onDismiss()
                    },
                    fillMaxWidth = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// Sección de selección única: tocar el seleccionado lo deselecciona (índice -1).
@Composable
private fun SingleChoiceSection(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
        QSectionHeader(title = title)
        QFilterChipGroup(
            options = options,
            selectedIndices = if (selectedIndex >= 0) setOf(selectedIndex) else emptySet(),
            onToggle = { index -> onSelect(if (index == selectedIndex) -1 else index) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QCatalogFilterSheet - con selecciones")
@Composable
private fun QCatalogFilterSheetPreview() {
    QarinoTheme {
        val visible = remember { mutableStateOf(true) }
        QCatalogFilterSheet(
            applied =
                FilterSelection(
                    tamanoIndex = 1,
                    edadIndex = 1,
                    sexoIndex = 0,
                    razaIndex = -1,
                    caracteristicas = setOf(0),
                ),
            options =
                FilterOptions(
                    tamanos = listOf("Chico", "Mediano", "Grande"),
                    edades = listOf("Cachorro", "Adulto", "Senior"),
                    sexos = listOf("Macho", "Hembra"),
                    razas = listOf("Mestizo", "Labrador", "Pastor alemán", "Chihuahua"),
                    caracteristicas = listOf("Convive con gatos", "Cuidados especiales"),
                ),
            onApply = {},
            onClear = {},
            onDismiss = { visible.value = false },
            isVisible = visible.value,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QCatalogFilterSheet - sin filtros")
@Composable
private fun QCatalogFilterSheetEmptyPreview() {
    QarinoTheme {
        QCatalogFilterSheet(
            applied = FilterSelection(),
            options =
                FilterOptions(
                    razas = listOf("Mestizo", "Labrador"),
                ),
            onApply = {},
            onClear = {},
            onDismiss = {},
            isVisible = true,
        )
    }
}
