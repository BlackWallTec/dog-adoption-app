package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QDatosGeneralesPage
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesViewModel

// ROUTE: conecta el DatosGeneralesViewModel con la página QDatosGeneralesPage (paso 2)
@Composable
fun DatosGeneralesRoute(
    onBack: () -> Unit, // regresar a Requisitos
    onIrATuHogar: () -> Unit, // navegar al paso 3
    viewModel: DatosGeneralesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listo by viewModel.listo.collectAsState()

    // Al entrar: carga del borrador (si ya llenó este paso) o del perfil
    LaunchedEffect(Unit) {
        viewModel.onLoadDatosGenerales()
    }

    // Guardado en el borrador → navegamos y avisamos
    LaunchedEffect(listo) {
        if (listo) {
            onIrATuHogar()
            viewModel.onNavegacionRealizada()
        }
    }

    val success = uiState as? DatosGeneralesUiState.Success

    QDatosGeneralesPage(
        isLoading = uiState is DatosGeneralesUiState.Loading,
        loadError = (uiState as? DatosGeneralesUiState.Error)?.mensaje,
        data = success?.datos,
        age = success?.edad,
        occupationError = success?.errores?.ocupacion,
        onOccupationChange = viewModel::onOcupacionChange,
        isSaving = false, // guardar es instantáneo (memoria)
        saveError = null, // y no puede fallar por red
        onRetry = { viewModel.onLoadDatosGenerales() },
        onBack = onBack,
        onContinue = { viewModel.onContinuar() },
    )
}