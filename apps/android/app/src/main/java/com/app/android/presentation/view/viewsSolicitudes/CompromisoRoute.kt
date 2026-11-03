package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.CompromisoScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel
import com.app.android.presentation.viewmodel.viewModelSolicitudes.GuardarUiState

// ROUTE: Conecta el CompromisoViewModel con la page CompromisoScreen
@Composable
fun CompromisoRoute(
    idSolicitud: String,
    onBack: () -> Unit, // Regresar a Tu Hogar
    onIrADocumentos: () -> Unit, // Navegar a Compromisos
    // 1. Hilt inyecta el ViewModel
    viewModel: CompromisoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardarState by viewModel.guardarState.collectAsState()

    // Cuando se entra se cargan las respuestas que se guardan
    LaunchedEffect(idSolicitud) {
        viewModel.onLoadCompromiso(idSolicitud)
    }

    // Si se guardo bien se navega y avisa

    LaunchedEffect(guardarState) {
        if (guardarState is GuardarUiState.Success) {
            onIrADocumentos()
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
        onDatosChange = viewModel::onDatosChange,
        guardando = guardarState is GuardarUiState.Loading,
        errorGuardar = (guardarState as? GuardarUiState.Error)?.mensaje,
        onReintentar = { viewModel.onLoadCompromiso(idSolicitud) },
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar(idSolicitud) },
    )
}
