package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginViewModel

/** Punto de conexión con la navegación: la vista previa usa LoginScreen directamente. */
@Composable
fun LoginRoute(
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
    onAuthenticated: (Sesion) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    LaunchedEffect(viewModel) {
        viewModel.authenticatedSessions.collect { currentOnAuthenticated(it) }
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onBackToCatalog = {
            viewModel.cancelLogin()
            onBackToCatalog()
        },
        onExploreWithoutAccount = {
            viewModel.cancelLogin()
            onExploreWithoutAccount()
        },
        onRegister = {
            viewModel.cancelLogin()
            onRegister()
        },
    )
}
