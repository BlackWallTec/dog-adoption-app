package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QMisSolicitudesPage
import com.app.android.presentation.view.atomicDesign.page.MisSolicitudesScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesViewModel

// Conecta MisSolicitudesViewModel con la page MisSolicitudesScreen
@Composable
fun MisSolicitudesRoute(
    onEditar: (idSolicitud: String) -> Unit, // la HU de editar documentos lo va a utilizar
    onVerCatalogo: () -> Unit,
    onNavSelect: (Int) -> Unit, // barra de abajo
    // 1. Hilt inyecta el ViewModel
    viewModel: MisSolicitudesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cada vez que se entra se piden las solicitudes
    LaunchedEffect(Unit) {
        viewModel.onLoadSolicitudes()
    }

    // Se traducen los tres estados a parametros para la page
    QMisSolicitudesPage(
        isLoading = uiState is MisSolicitudesUiState.Loading,
        errorMessage = (uiState as? MisSolicitudesUiState.Error)?.mensaje,
        requests = (uiState as? MisSolicitudesUiState.Success)?.solicitudes.orEmpty(),
        onRetry = { viewModel.onLoadSolicitudes() },
        onEditClick = onEditar,
        onCatalogClick = onVerCatalogo,
        onTabSelected = { onNavSelect(it.ordinal) },
    )
}
