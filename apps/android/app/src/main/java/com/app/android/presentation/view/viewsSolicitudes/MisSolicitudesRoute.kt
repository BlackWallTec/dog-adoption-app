package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.presentation.view.atomicDesign.page.MisSolicitudesScreen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.EdicionUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesViewModel

// Conecta MisSolicitudesViewModel con la page MisSolicitudesScreen
@Composable
fun MisSolicitudesRoute(
    idUsuario: String, // sale de la sesión
    onIrAConfirmar: () -> Unit, // el borrador esta listo
    onVerCatalogo: () -> Unit,
    onNavSelect: (Int) -> Unit, // barra de abajo
    // 1. Hilt inyecta el ViewModel
    viewModel: MisSolicitudesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val edicionState by viewModel.edicionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cada vez que se entra se piden las solicitudes
    LaunchedEffect(Unit) {
        viewModel.onLoadSolicitudes()
    }

    LaunchedEffect(edicionState) {
        when (val estado = edicionState) {
            is EdicionUiState.Lista -> {
                onIrAConfirmar()
                viewModel.onEdicionAtendida()
            }

            is EdicionUiState.NoEditable -> {
                snackbarHostState.showSnackbar(estado.mensaje)
                viewModel.onEdicionAtendida()
            }

            is EdicionUiState.Error -> {
                snackbarHostState.showSnackbar(estado.mensaje)
                viewModel.onEdicionAtendida()
            }

            else -> {
                Unit
            }
        }
    }

    // Se traducen los tres estados a parámetros para la page
    val success = uiState as? MisSolicitudesUiState.Success
    MisSolicitudesScreen(
        cargando = uiState is MisSolicitudesUiState.Loading,
        errorCarga = (uiState as? MisSolicitudesUiState.Error)?.mensaje,
        solicitudes = success?.solicitudes.orEmpty(),
        onReintentar = { viewModel.onLoadSolicitudes() },
        onEditarClick = { idSolicitud -> viewModel.onEditar(idUsuario, idSolicitud) },
        onVerCatalogoClick = onVerCatalogo,
        onNavSelect = onNavSelect,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}
