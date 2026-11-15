package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.CompromisoScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel

// ROUTE: Conecta el CompromisoViewModel con la page CompromisoScreen
@Composable
fun CompromisoRoute(
    onBack: () -> Unit, // Regresar a Tu Hogar
    onIrADocumentoss: () -> Unit, // Navegar a Compromisos
    // 1. Hilt inyecta el ViewModel
    viewModel: CompromisoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listo by viewModel.listo.collectAsState()
    val acceptances by viewModel.acceptances.collectAsState()

    LaunchedEffect(Unit) { viewModel.onLoadCompromiso() }

    // Cuando se entra se cargan las respuestas que se guardan
    LaunchedEffect(listo) {
        if (listo) {
            onIrADocumentoss()
            viewModel.onNavegacionRealizada()
        }
    }

    // Se traducen los estados a parametros para la page

    val success = uiState as? CompromisoUiState.Success

    CompromisoScreen(
        cargando = uiState is CompromisoUiState.Loading,
        errorCarga = (uiState as? CompromisoUiState.Error)?.mensaje,
        datos = success?.datos,
        errores = success?.errores,
        acceptances = acceptances,
        onAcceptancesChange = viewModel::onAcceptancesChange,
        onDatosChange = viewModel::onDatosChange,
        guardando = false,
        errorGuardar = null,
        onReintentar = { viewModel.onLoadCompromiso() },
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar() },
    )
}
