package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.DatosGeneralesScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesViewModel
import com.app.android.presentation.viewmodel.viewModelSolicitudes.GuardarUiState

// ROUTE: conecta el DatosGeneralesViewModel con la page DatosGeneralesScreen
@Composable
fun DatosGeneralesRoute(
    idSolicitud: String,
    idUsuario: String,
    onBack: () -> Unit, // regresar a Requisitos
    onIrATuHogar: () -> Unit, // navegar al paso 3
    viewModel: DatosGeneralesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardarState by viewModel.guardarState.collectAsState()

    // 1 cuando se entra se cargan los datos
    LaunchedEffect(idSolicitud) {
        viewModel.onLoadDatosGenerales(idSolicitud, idUsuario)
    }

    // 2 Si se guardo bien se navega y avisa
    LaunchedEffect(guardarState) {
        if (guardarState is GuardarUiState.Success) {
            onIrATuHogar()
            viewModel.onNavegacionRealizada()
        }
    }

    // 3 Traduccion los dos estados a parametros para la page
    val success = uiState as? DatosGeneralesUiState.Success

    DatosGeneralesScreen(
        cargando = uiState is DatosGeneralesUiState.Loading,
        errorCarga = (uiState as? DatosGeneralesUiState.Error)?.mensaje,
        datos = success?.datos,
        edad = success?.edad,
        errorOcupacion = success?.errores?.ocupacion,
        onOcupacionChange = viewModel::onOcupacionChange,
        guardando = guardarState is GuardarUiState.Loading,
        errorGuardar = (guardarState as? GuardarUiState.Error)?.mensaje,
        onReintentar = { viewModel.onLoadDatosGenerales(idSolicitud, idUsuario) },
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar(idSolicitud) },
    )
}