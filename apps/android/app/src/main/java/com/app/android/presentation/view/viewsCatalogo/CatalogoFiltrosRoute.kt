package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.presentation.viewmodel.viewModelCatalogo.CatalogFiltersViewModel

// Conecta el ViewModel de filtros con la pantalla (HU24)
@Composable
fun CatalogoFiltrosRoute(
    onDogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogFiltersViewModel = hiltViewModel(),
    onNavSelect: (Int) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val filtros by viewModel.appliedFilters.collectAsState()
    val opciones by viewModel.options.collectAsState()

    CatalogoFiltradoScreen(
        state = state,
        appliedFilters = filtros,
        options = opciones,
        onApplyFilters = viewModel::onApplyFilters,
        onClearFilters = viewModel::onClearFilters,
        onRetry = viewModel::onRetry,
        onDogClick = onDogClick,
        onNavSelect = onNavSelect,
        modifier = modifier,
    )
}
