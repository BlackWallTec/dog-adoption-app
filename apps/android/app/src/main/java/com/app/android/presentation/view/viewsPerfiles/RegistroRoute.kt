package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
    val currentOnRegistered by rememberUpdatedState(onRegistered)

    LaunchedEffect(viewModel) {
        viewModel.registrationCompleted.collect {
            currentOnRegistered()
        }
    }

    RegistroScreen(
        state = state,
        onChange = viewModel::actualizar,
        onPrivacyChange = viewModel::privacidad,
        onRegister = viewModel::registrar,
        onCancel = {
            viewModel.cancelar()
            onCancel()
        },
    )
}
