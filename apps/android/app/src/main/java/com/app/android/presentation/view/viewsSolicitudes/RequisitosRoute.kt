package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.designsystem.pages.QRequisitosPage
import com.app.android.presentation.viewmodel.viewModelSolicitudes.RequisitosUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.RequisitosViewModel

private const val MENSAJE_BLOQUEADA =
    "Ya tienes una solicitud activa para este perro. Puedes revisarla en Solicitudes."

// ROUTE: conecta el RequisitosViewModel con la página QRequisitosPage (paso 1)
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

    // Borrador iniciado → navegamos y avisamos
    LaunchedEffect(uiState) {
        if (uiState is RequisitosUiState.Lista) {
            onIrADatosGenerales()
            viewModel.onNavegacionRealizada()
        }
    }

    // Traducimos el estado a un mensaje para la página
    val mensaje =
        when (val estado = uiState) {
            is RequisitosUiState.Error -> estado.mensaje
            is RequisitosUiState.Bloqueada -> MENSAJE_BLOQUEADA
            else -> null
        }

    QRequisitosPage(
        accepted = aceptado,
        onAcceptedChange = viewModel::onAceptadoChange,
        isLoading = uiState is RequisitosUiState.Loading,
        errorMessage = mensaje,
        onBack = onBack,
        onContinue = { viewModel.onContinuar(idUsuario, idPerro) },
    )
}