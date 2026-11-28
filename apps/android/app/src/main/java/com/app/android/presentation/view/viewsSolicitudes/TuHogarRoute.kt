package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QTuHogarPage
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarViewModel

// ROUTE: conecta el TuHogarViewModel con la página QTuHogarPage (paso 3)
@Composable
fun TuHogarRoute(
    onBack: () -> Unit, // Regresar a Datos Generales
    onIrACompromiso: () -> Unit, // Navegar a Compromiso
    viewModel: TuHogarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listo by viewModel.listo.collectAsState()

    // Cuando se entra se cargan las respuestas ya guardadas
    LaunchedEffect(Unit) {
        viewModel.onLoadTuHogar()
    }

    // Si se guardó bien se navega y avisa
    LaunchedEffect(listo) {
        if (listo) {
            onIrACompromiso()
            viewModel.onNavegacionRealizada()
        }
    }

    val success = uiState as? TuHogarUiState.Success

    // petName se queda con su valor por defecto: el ViewModel no expone el nombre del perro
    QTuHogarPage(
        isLoading = uiState is TuHogarUiState.Loading,
        loadError = (uiState as? TuHogarUiState.Error)?.mensaje,
        data = success?.datos,
        errors = success?.errores,
        onDataChange = viewModel::onDatosChange,
        isSaving = false, // guardar es instantáneo (memoria)
        saveError = null, // y no puede fallar por red
        onRetry = { viewModel.onLoadTuHogar() },
        onBack = onBack,
        onContinue = { viewModel.onContinuar() },
    )
}