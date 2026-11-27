package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.organisms.QCatalogFilterSheet
import com.app.android.presentation.designsystem.organisms.QPetCard
import com.app.android.presentation.designsystem.organisms.QSearchBar
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QMainTabTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogCardUi
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterOptions
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterSelection

/**
 * Catálogo de perritos (pestaña Catálogo): buscador con botón de filtros arriba y grid de 2 columnas con
 * QPetCard. Maneja cargando, error (Reintentar) y vacío (distingue "sin perros" de "sin coincidencias").
 * El texto del buscador es estado visual local y filtra por nombre la lista que ya llegó.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QMainTabTemplate, QSearchBar, QPetCard, QCatalogFilterSheet, QStateContent.
 * Reemplaza a: CatalogoFiltradoScreen (CatalogoFiltrosRoute).
 */
@Composable
fun QCatalogoPage(
    dogs: List<DogCardUi>,
    isLoading: Boolean,
    errorMessage: String?,
    appliedFilters: FilterSelection,
    filterOptions: FilterOptions,
    onApplyFilters: (FilterSelection) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onDogClick: (dogId: String) -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    val trimmedQuery = query.trim()
    val visibleDogs =
        if (trimmedQuery.isEmpty()) {
            dogs
        } else {
            dogs.filter { it.nombre.contains(trimmedQuery, ignoreCase = true) }
        }
    val hasFilters = !appliedFilters.isEmpty

    QMainTabTemplate(
        title = "Catálogo",
        subtitle = "Encuentra a tu nuevo mejor amigo.",
        selectedTab = QTab.Catalogo,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        scrollable = false,
    ) {
        QSearchBar(
            query = query,
            onQueryChange = { query = it },
            onFilterClick = { showFilters = true },
        )
        if (hasFilters) {
            QText(
                text = "Filtros aplicados: ${appliedFilters.activeCount}",
                style = QTextStyle.Caption,
            )
        }
        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            modifier = Modifier.weight(1f),
            isEmpty = visibleDogs.isEmpty(),
            emptyMessage = emptyMessage(hasQuery = trimmedQuery.isNotEmpty(), hasFilters = hasFilters),
            emptyActionText = if (hasFilters) "Quitar filtros" else null,
            onEmptyAction = if (hasFilters) onClearFilters else null,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
                verticalArrangement = Arrangement.spacedBy(QSpacing.m),
            ) {
                items(items = visibleDogs, key = { it.id }) { dog ->
                    QPetCard(
                        photo = dog.fotoUrl,
                        name = dog.nombre,
                        age = dog.edad,
                        sex = dog.sexo,
                        onClick = { onDogClick(dog.id) },
                    )
                }
            }
        }
    }

    QCatalogFilterSheet(
        applied = appliedFilters,
        options = filterOptions,
        onApply = onApplyFilters,
        onClear = onClearFilters,
        onDismiss = { showFilters = false },
        isVisible = showFilters,
    )
}

// Distingue "no hay perros" de "tu búsqueda o tus filtros no encontraron nada".
private fun emptyMessage(
    hasQuery: Boolean,
    hasFilters: Boolean,
): String =
    when {
        hasQuery -> "Ningún perro coincide con tu búsqueda"
        hasFilters -> "Ningún perro coincide con tus filtros"
        else -> "No hay perros disponibles"
    }

private val previewDogs =
    listOf(
        DogCardUi("1", "Firulais", "3 años", "Macho", "Chico", "Mestizo"),
        DogCardUi("2", "Canela", "1 año", "Hembra", "Mediano", "Labrador"),
        DogCardUi("3", "Pelusa con un nombre muy largo para probar", "Menos de 1 año", "Hembra", "Chico", "Chihuahua"),
        DogCardUi("4", "Chispa", "5 años", "Macho", "Grande", "Pastor"),
    )

private val previewOptions =
    FilterOptions(
        razas = listOf("Mestizo", "Labrador", "Chihuahua"),
        caracteristicas = listOf("Convive con gatos", "Cuidados especiales"),
    )

@Composable
private fun PreviewCatalogo(
    dogs: List<DogCardUi> = previewDogs,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    filters: FilterSelection = FilterSelection(),
) {
    QarinoTheme {
        QCatalogoPage(
            dogs = dogs,
            isLoading = isLoading,
            errorMessage = errorMessage,
            appliedFilters = filters,
            filterOptions = previewOptions,
            onApplyFilters = {},
            onClearFilters = {},
            onRetry = {},
            onDogClick = {},
            onTabSelected = {},
            notificationCount = 2,
        )
    }
}

@Preview(showBackground = true, heightDp = 800, name = "QCatalogoPage - lista (filtros: botón Run)")
@Composable
private fun QCatalogoPagePreview() {
    PreviewCatalogo(filters = FilterSelection(sexoIndex = 1))
}

@Preview(showBackground = true, heightDp = 800, name = "QCatalogoPage - cargando")
@Composable
private fun QCatalogoPageLoadingPreview() {
    PreviewCatalogo(isLoading = true)
}

@Preview(showBackground = true, heightDp = 800, name = "QCatalogoPage - sin coincidencias")
@Composable
private fun QCatalogoPageEmptyPreview() {
    PreviewCatalogo(dogs = emptyList(), filters = FilterSelection(tamanoIndex = 2))
}

@Preview(showBackground = true, heightDp = 800, name = "QCatalogoPage - error")
@Composable
private fun QCatalogoPageErrorPreview() {
    PreviewCatalogo(errorMessage = "No pudimos cargar los perros. Revisa tu conexión e intenta de nuevo.")
}
