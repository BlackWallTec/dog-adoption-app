package com.app.android.presentation.view.viewsDonativos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.MisDonativosScreen
import com.app.android.presentation.viewmodel.viewModelDonativos.MisDonativosViewModel

// ROUTE: conecta el MisDonativosViewModel con la page MisDonativosScreen (HU49)
@Composable
fun MisDonativosRoute(
    onBack: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    viewModel: MisDonativosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    MisDonativosScreen(
        state = uiState,
        onBackClick = onBack,
        onReintentar = viewModel::cargar,
        onPedirCfdi = viewModel::onPedirCfdi,
        onNavSelect = onNavSelect,
    )
}