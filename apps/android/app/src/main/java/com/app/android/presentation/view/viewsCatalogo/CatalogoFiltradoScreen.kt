package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.app.android.presentation.view.atomicDesign.organism.FilterSheet
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.SearchBar
import com.app.android.presentation.viewmodel.viewModelCatalogo.CatalogUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogCardUi
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterOptions
import com.app.android.presentation.viewmodel.viewModelCatalogo.FilterSelection

// Pantalla "tonta" del catálogo con filtros (HU24): solo pinta lo que le pasa la Route.
@Composable
fun CatalogoFiltradoScreen(
    state: CatalogUiState,
    appliedFilters: FilterSelection,
    options: FilterOptions,
    onApplyFilters: (FilterSelection) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onLoadNextPage: () -> Unit = {},
    onDogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavSelect: (Int) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) } // navbar
    var query by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBF2))
                .statusBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                }
            }
            Text("Catálogo", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Encuentra a tu nuevo mejor amigo.", fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))

            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onFilterClick = { showSheet = true },
            )

            if (!appliedFilters.isEmpty) {
                Spacer(Modifier.height(8.dp))
                Text("Filtros aplicados: ${appliedFilters.activeCount}", fontSize = 14.sp)
            }

            CatalogoContenido(
                state = state,
                hayFiltros = !appliedFilters.isEmpty,
                onClearFilters = onClearFilters,
                onRetry = onRetry,
                onLoadNextPage = onLoadNextPage,
                onDogClick = onDogClick,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            )
        }
        NavBar(
            selected = selected,
            onSelect = { opcion ->
                if (opcion == 0) {
                    selected = 0
                } else {
                    onNavSelect(opcion)
                }
            },
        )
    }

    FilterSheet(
        applied = appliedFilters,
        options = options,
        onApply = onApplyFilters,
        onClear = onClearFilters,
        onDismiss = { showSheet = false },
        isVisible = showSheet,
    )
}

@Composable
private fun CatalogoContenido(
    state: CatalogUiState,
    hayFiltros: Boolean,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onLoadNextPage: () -> Unit,
    onDogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CatalogUiState.Loading, CatalogUiState.LoadingMore -> {
            // Caso 1: Cargando la primera página
            if (state is CatalogUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Caso 2: Cargando la siguiente página
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Surface(
                        modifier = Modifier.padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 6.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = "Cargando más perritos...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        CatalogUiState.Empty -> {
            Column(
                modifier = modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Distingue "no hay perros" de "los filtros no encontraron nada"
                Text(
                    text =
                        if (hayFiltros) {
                            "Ningún perro coincide con tus filtros"
                        } else {
                            "No hay perros disponibles"
                        },
                    textAlign = TextAlign.Center,
                )
                if (hayFiltros) {
                    TextButton(onClick = onClearFilters) { Text("Quitar filtros") }
                }
            }
        }

        is CatalogUiState.Error -> {
            Column(
                modifier = modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.mensaje, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
        }

        is CatalogUiState.Success -> {
            val gridState = rememberLazyGridState()

            // Detectar cuando se llega casi al final del Scroll
            LaunchedEffect(gridState, state.perros.size) {
                snapshotFlow {
                    gridState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index
                }.collect { lastVisibleIndex ->
                    // Si el último elemento visible está entre los últimos 2 de la lista
                    if (lastVisibleIndex != null && lastVisibleIndex >= state.perros.size - 2) {
                        onLoadNextPage()
                    }
                }
            }

            LazyVerticalGrid(
                state = gridState,
                modifier = modifier,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.perros, key = { it.id }) { perro ->
                    Card(onClick = { onDogClick(perro.id) }) {
                        Column {
                            // Imagen del perro
                            AsyncImage(
                                model = perro.fotoUrl,
                                contentDescription = perro.nombre,
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(com.app.android.R.drawable.logo),
                                error = painterResource(com.app.android.R.drawable.logo),
                                fallback = painterResource(com.app.android.R.drawable.logo),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                            )

                            Column(Modifier.padding(12.dp)) {
                                Text(perro.nombre, fontWeight = FontWeight.Bold)
                                // perro.edad ya trae la unidad ("1 año", "3 años")
                                Text(perro.edad)
                                Text(perro.sexo)
                            }
                        }
                    }
                }

                // Muestra mensaje final si no hay más perros
                if (!state.hasMore && state.perros.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "✨ Llegaste al fin del catálogo ✨",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF795548),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "¡Anímate y adopta a tu nuevo mejor amigo! 🐶❤️",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val perrosDePrueba =
    listOf(
        DogCardUi("1", "Kira", "3 años", "Hembra", "Chico", "Mestizo"),
        DogCardUi("2", "Abraham", "1 año", "Macho", "Mediano", "Labrador"),
        DogCardUi("3", "Alexa", "Menos de 1 año", "Hembra", "Chico", "Chihuahua"),
    )

private val opcionesDePrueba =
    FilterOptions(
        razas = listOf("Mestizo", "Labrador", "Chihuahua"),
        caracteristicas = listOf("Convive con gatos", "Cuidados especiales"),
    )

@Preview(widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun CatalogoFiltradoSuccessPreview() {
    CatalogoFiltradoScreen(
        state = CatalogUiState.Success(perrosDePrueba),
        appliedFilters = FilterSelection(sexoIndex = 1),
        options = opcionesDePrueba,
        onApplyFilters = {},
        onClearFilters = {},
        onRetry = {},
        onDogClick = {},
    )
}

@Preview(widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun CatalogoFiltradoSinResultadosPreview() {
    CatalogoFiltradoScreen(
        state = CatalogUiState.Empty,
        appliedFilters = FilterSelection(tamanoIndex = 2, edadIndex = 0),
        options = opcionesDePrueba,
        onApplyFilters = {},
        onClearFilters = {},
        onRetry = {},
        onDogClick = {},
    )
}

@Preview(widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun CatalogoFiltradoErrorPreview() {
    CatalogoFiltradoScreen(
        state = CatalogUiState.Error("No pudimos cargar los perros. Revisa tu conexión e intenta de nuevo."),
        appliedFilters = FilterSelection(),
        options = opcionesDePrueba,
        onApplyFilters = {},
        onClearFilters = {},
        onRetry = {},
        onDogClick = {},
    )
}
