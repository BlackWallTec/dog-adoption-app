package com.app.android.presentation.view.viewsPerfiles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
    val currentOnAuthenticated by rememberUpdatedState(onAuthenticated)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recibo el resultado mientras esta pantalla está activa.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.authenticatedSessions.collect { currentOnAuthenticated(it) }
        }
    }

    // El botón de atrás del teléfono también cancela el intento de login.
    BackHandler {
        viewModel.cancelLogin()
        onBackToCatalog()
    }

    // Al salir, limpio la contraseña y cancelo la petición que pudiera seguir.
    DisposableEffect(viewModel) {
        onDispose { viewModel.cancelLogin() }
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
