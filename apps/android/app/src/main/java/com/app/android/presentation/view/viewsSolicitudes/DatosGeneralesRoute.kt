package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.DatosGeneralesScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DatosGeneralesViewModel

// ROUTE: conecta el DatosGeneralesViewModel con la page DatosGeneralesScreen
@Composable
fun DatosGeneralesRoute(
    onBack: () -> Unit, // regresar a Requisitos
    onIrATuHogar: () -> Unit, // navegar al paso 3
    viewModel: DatosGeneralesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listo by viewModel.listo.collectAsState()

    // Al entrar: carga del borrador (si ya lleno este paso) o del perfil
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

    DatosGeneralesScreen(
        cargando = uiState is DatosGeneralesUiState.Loading,
        errorCarga = (uiState as? DatosGeneralesUiState.Error)?.mensaje,
        datos = success?.datos,
        edad = success?.edad,
        errorOcupacion = success?.errores?.ocupacion,
        onOcupacionChange = viewModel::onOcupacionChange,
        guardando = false, // guardar es instanttt (memoria)
        errorGuardar = null, // y no puede fallar por red
        onReintentar = { viewModel.onLoadDatosGenerales() },
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar() },
    )
}