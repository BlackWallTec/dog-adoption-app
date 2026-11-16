package com.app.android.presentation.view.viewsPerfiles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el ViewModel pone una sesión lista, navegamos y limpiamos el evento.
    LaunchedEffect(state.sesionLista) {
        val sesion = state.sesionLista ?: return@LaunchedEffect
        viewModel.consumirSesion()
        onAuthenticated(sesion)
    }

    // El botón de atrás del teléfono también cancela el intento de login.
    BackHandler {
        viewModel.cancelLogin()
        onBackToCatalog()
    }

    LoginScreen(
        state             = state,
        onEmailChange     = viewModel::onEmailChange,
        onPasswordChange  = viewModel::onPasswordChange,
        onLogin           = viewModel::login,
        onBackToCatalog   = {
            viewModel.cancelLogin()
            onBackToCatalog()
        },
        onExploreWithoutAccount = {
            viewModel.cancelLogin()
            onExploreWithoutAccount()
        },
        onRegister        = {
            viewModel.cancelLogin()
            onRegister()
        },
    )
}