package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.viewmodel.viewModelSolicitudes.AdoptionFormViewModel

@Composable
fun AdoptionFormScreen(
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    // 1. Hilt inyecta el ViewModel
    viewModel: AdoptionFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el envio sale bien, avisamos a quien naveg
    // Nos suscribimos al UiState
    LaunchedEffect(state.isSubmitted) {
        // Cuando el envio sale bien, avisamos a quien navega
        if (state.isSubmitted) {
            onSubmitted()
        }
    }

    AdoptionFormContent(
        state = state,
        onBackClick = onBackClick,
        onFormChange = viewModel::onFormChange,
        onSubmitClick = viewModel::onSubmitClick,
    )
}