package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.designsystem.pages.QCatalogoPage
import com.app.android.presentation.viewmodel.viewModelCatalogo.CatalogFiltersViewModel
import com.app.android.presentation.viewmodel.viewModelCatalogo.CatalogUiState

// Conecta el ViewModel de filtros con la página del catálogo (HU24)
@Composable
fun CatalogoFiltrosRoute(
    onDogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogFiltersViewModel = hiltViewModel(),
    onNavSelect: (Int) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val appliedFilters by viewModel.appliedFilters.collectAsStateWithLifecycle()
    val filterOptions by viewModel.options.collectAsStateWithLifecycle()

    QCatalogoPage(
        dogs = (state as? CatalogUiState.Success)?.perros.orEmpty(),
        isLoading = state is CatalogUiState.Loading,
        errorMessage = (state as? CatalogUiState.Error)?.mensaje,
        appliedFilters = appliedFilters,
        filterOptions = filterOptions,
        onApplyFilters = viewModel::onApplyFilters,
        onClearFilters = viewModel::onClearFilters,
        onRetry = viewModel::onRetry,
        onDogClick = onDogClick,
        onTabSelected = { onNavSelect(it.ordinal) },
        modifier = modifier,
    )
}