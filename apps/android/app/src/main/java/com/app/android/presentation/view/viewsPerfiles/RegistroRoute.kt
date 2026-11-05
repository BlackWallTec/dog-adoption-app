package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroViewModel

@Composable
fun RegistroRoute(onCancel: () -> Unit, onAuthenticated: (Sesion) -> Unit, viewModel: RegistroViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    LaunchedEffect(viewModel) { viewModel.authenticatedSessions.collect { currentOnAuthenticated(it) } }
    RegistroScreen(state, viewModel::actualizar, viewModel::privacidad, viewModel::registrar, {
        viewModel.cancelar()
        onCancel()
    })
}
