package com.app.android.presentation.view.viewsDonativos

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QMisDonativosPage
import com.app.android.presentation.viewmodel.viewModelDonativos.MisDonativosViewModel

// ROUTE: conecta el MisDonativosViewModel con la page MisDonativosScreen (HU49)
@Composable
fun MisDonativosRoute(
    onBack: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    viewModel: MisDonativosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    QMisDonativosPage(
        isLoading = uiState.cargando,
        errorMessage = uiState.error,
        donations = uiState.donativos,
        onBack = onBack,
        onRetry = viewModel::cargar,
        onRequestCfdi = viewModel::onPedirCfdi,
        onTabSelected = { onNavSelect(it.ordinal) },
        sendingCfdiId = uiState.enviandoCfdi,
        infoMessage = uiState.mensaje,
    )
}