package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.RequisitosScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.RequisitosUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.RequisitosViewModel

// ROUTE: conecta el RequisitosViewModel con la page RequisitosScreen
@Composable
fun RequisitosRoute(
    idUsuario: String,
    idPerro: String,
    onBack: () -> Unit, // regresar (a Catálogo)
    onIrADatosGenerales: (idSolicitud: String) -> Unit, // navegar al paso 2
    viewModel: RequisitosViewModel = hiltViewModel(),
) {
    // 1 se observan los dos estados del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val guardarState by viewModel.guardarState.collectAsState()

    // 2 Si se creo la solicitud se navega y avisa que ya navego
    LaunchedEffect(uiState) {
        val estado = uiState
        if (estado is RequisitosUiState.Success) {
            onIrADatosGenerales(estado.idSolicitud)
            viewModel.onNavegacionRealizada()
        }
    }

    // 3 se traduce el estado a parametros simples para la page
    RequisitosScreen(
        aceptado = aceptado,
        onAceptadoChange = viewModel::onAceptadoChange,
        cargando = uiState is RequisitosUiState.Loading,
        errorMensaje = (uiState as? RequisitosUiState.Error)?.mensaje,
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar(idUsuario, idPerro) },
    )
}
