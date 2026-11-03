package com.app.android.presentation.view.viewsDonativos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.DatosFiscalesScreen
import com.app.android.presentation.viewmodel.viewModelDonativos.DatosFiscalesUiState
import com.app.android.presentation.viewmodel.viewModelDonativos.DatosFiscalesViewModel
import com.app.android.presentation.viewmodel.viewModelDonativos.GuardarDatosFiscalesUiState

// ROUTE: conecta el DatosFiscalesViewModel con la page DatosFiscalesScreen (HU45)
@Composable
fun DatosFiscalesRoute(
    idUsuario: String,
    onBack: () -> Unit,
    viewModel: DatosFiscalesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardarState by viewModel.guardarState.collectAsState()

    // 1 cuando se entra se cargan los datos
    LaunchedEffect(idUsuario) {
        viewModel.onLoadDatosFiscales(idUsuario)
    }

    // 2 Traduccion de los dos estados a parametros para la page
    val success = uiState as? DatosFiscalesUiState.Success

    DatosFiscalesScreen(
        cargando = uiState is DatosFiscalesUiState.Loading,
        errorCarga = (uiState as? DatosFiscalesUiState.Error)?.mensaje,
        datos = success?.datos,
        yaGuardados = success?.yaGuardados ?: false,
        constanciaNueva = success?.constanciaNueva?.nombre,
        infoConstancia = success?.infoConstancia ?: "",
        errores = success?.errores,
        guardando = guardarState is GuardarDatosFiscalesUiState.Loading,
        guardadoOk = guardarState is GuardarDatosFiscalesUiState.Success,
        errorGuardar = (guardarState as? GuardarDatosFiscalesUiState.Error)?.mensaje,
        onRfcChange = viewModel::onRfcChange,
        onRazonSocialChange = viewModel::onRazonSocialChange,
        onRegimenChange = viewModel::onRegimenChange,
        onCodigoPostalChange = viewModel::onCodigoPostalChange,
        onCorreoChange = viewModel::onCorreoChange,
        onConstanciaSeleccionada = viewModel::onConstanciaSeleccionada,
        onConstanciaInvalida = viewModel::onConstanciaInvalida,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onReintentar = { viewModel.onLoadDatosFiscales(idUsuario) },
        onGuardarClick = { viewModel.onGuardar(idUsuario) },
        onBackClick = onBack,
    )
}
