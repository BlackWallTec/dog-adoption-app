package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroViewModel

@Composable
fun RegistroRoute(
    onCancel: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el ViewModel marca el registro como exitoso, navegamos y limpiamos el flag.
    LaunchedEffect(state.registroExitoso) {
        if (!state.registroExitoso) return@LaunchedEffect
        viewModel.consumirRegistro()
        onRegistered()
    }

    RegistroScreen(
        state                      = state,
        onChange                   = viewModel::actualizar,
        onPrivacyChange            = viewModel::privacidad,
        onRegister                 = viewModel::registrar,
        opcionesCodigoPostal       = state.colonias.map {
            OpcionCodigoPostalUi(it.idCodigoPostal, it.codigoPostal, it.colonia, it.municipio, it.estado)
        },
        idCodigoPostalSeleccionado = state.datos.idCodigoPostal.ifBlank { null },
        cargandoColonias           = state.cargandoColonias,
        errorColonias              = state.errorColonias,
        onCodigoPostalSeleccionado = viewModel::seleccionarColonia,
        onCancel                   = {
            viewModel.cancelar()
            onCancel()
        },
    )
}