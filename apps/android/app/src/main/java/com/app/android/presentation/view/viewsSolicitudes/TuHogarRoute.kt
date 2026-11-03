package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.TuHogarScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarViewModel

// Conecta TuHogar viewModel con la page TuHogar
@Composable
fun TuHogarRoute(
    idSolicitud: String,
    onBack: () -> Unit, // Regresar a Datos Generales
    onIrACompromiso: () -> Unit, // Navegar a Compromisos
    // 1. Hilt inyecta el ViewModel
    viewModel: TuHogarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val state by viewModel.guardarState.collectAsState()

    // Cuando se entra se cargan las respuestas ya guardadas
    LaunchedEffect(idSolicitud) {
        viewModel.onLoadTuHogar(idSolicitud)
    }
    // Si se guardo bien se navega y avisa
    LaunchedEffect(guardarState) {
        if (guardarState is GuardarUiState.Success) {
            onIrACompromiso()
            viewModel.onNavegacionRealizada()
        }
    }

    // Se traducen los dos estados a paametros para la page

    val success = uiState as? TuHogarUiState.Success
    TuHogarScreen(
        cargando = uiState is TuHogarUiState.Loading,
        errorCarga = (uiState as? TuHogarUiState.Error)?.mensaje,
        datos = success?.datos,
        errores = success?.errores,
        onDatosChange = viewModel::onDatosChange,
        guardando = guardarState is GuardarUiState.Loading,
        errorGuardar = (guardarState as? GuardarUiState.Error)?.mensaje,
        onReintentar = { viewModel.onLoadTuHogar(idSolicitud) },
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar(idSolicitud) },
    )
}
