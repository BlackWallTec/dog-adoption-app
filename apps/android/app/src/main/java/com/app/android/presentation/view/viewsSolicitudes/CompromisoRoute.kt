package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QCompromisoPage
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel

// ROUTE: conecta el CompromisoViewModel con la página QCompromisoPage (paso 4)
@Composable
fun CompromisoRoute(
    onBack: () -> Unit, // Regresar a Tu Hogar
    onIrADocumentoss: () -> Unit, // Navegar a Documentos
    viewModel: CompromisoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listo by viewModel.listo.collectAsState()
    val acceptances by viewModel.acceptances.collectAsState()

    // Cuando se entra se cargan las respuestas guardadas
    LaunchedEffect(Unit) {
        viewModel.onLoadCompromiso()
    }

    // Si se guardó bien se navega y avisa
    LaunchedEffect(listo) {
        if (listo) {
            onIrADocumentoss()
            viewModel.onNavegacionRealizada()
        }
    }

    val success = uiState as? CompromisoUiState.Success

    // petName se queda con su valor por defecto: el ViewModel no expone el nombre del perro
    QCompromisoPage(
        isLoading = uiState is CompromisoUiState.Loading,
        loadError = (uiState as? CompromisoUiState.Error)?.mensaje,
        data = success?.datos,
        errors = success?.errores,
        onDataChange = viewModel::onDatosChange,
        acceptances = acceptances,
        onAcceptancesChange = viewModel::onAcceptancesChange,
        isSaving = false, // guardar es instantáneo (memoria)
        saveError = null, // y no puede fallar por red
        onRetry = { viewModel.onLoadCompromiso() },
        onBack = onBack,
        onContinue = { viewModel.onContinuar() },
    )
}