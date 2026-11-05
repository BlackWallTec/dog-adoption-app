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
    onBack: () -> Unit, // regresar (a Catalogo)
    onIrADatosGenerales: () -> Unit, // navegar al paso 2 (ya no lleva id: todo está en el borrador)
    viewModel: RequisitosViewModel = hiltViewModel(),
) {
    val aceptado by viewModel.aceptado.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Borrador iniciado → navegamos y avisamos (el papelito)
    LaunchedEffect(uiState) {
        if (uiState is RequisitosUiState.Lista) {
            onIrADatosGenerales()
            viewModel.onNavegacionRealizada()
        }
    }

    // Traducimos el estado a un mensaje para la page
    val mensaje =
        when (val estado = uiState) {
            is RequisitosUiState.Error -> estado.mensaje
            is RequisitosUiState.Bloqueada ->
                "Ya tienes una solicitud activa para este perro. Puedes revisarla en Solicitudes."
            else -> null
        }

    RequisitosScreen(
        aceptado = aceptado,
        onAceptadoChange = viewModel::onAceptadoChange,
        cargando = uiState is RequisitosUiState.Loading,
        errorMensaje = mensaje,
        onBackClick = onBack,
        onContinuarClick = { viewModel.onContinuar(idUsuario, idPerro) },
    )
}