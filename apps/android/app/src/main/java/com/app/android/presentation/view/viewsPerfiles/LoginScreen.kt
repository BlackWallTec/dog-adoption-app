package com.app.android.presentation.view.viewsPerfiles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.R
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.SupportingText
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginViewModel

@Composable
fun LoginScreen(
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
    onAuthenticated: (Sesion) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.sesionLista) {
        state.sesionLista?.let { sesion ->
            viewModel.consumirSesion()
            onAuthenticated(sesion)
        }
    }
    BackHandler { viewModel.cancelLogin(); onBackToCatalog() }
    LoginContent(
        state, viewModel::onEmailChange, viewModel::onPasswordChange, viewModel::login,
        onBackToCatalog = { viewModel.cancelLogin(); onBackToCatalog() },
        onExploreWithoutAccount = { viewModel.cancelLogin(); onExploreWithoutAccount() },
        onRegister = { viewModel.cancelLogin(); onRegister() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
) {
    val focus = LocalFocusManager.current
    var visible by rememberSaveable { mutableStateOf(false) }
    val submit = { focus.clearFocus(); onLogin() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(title = { Text("Iniciar sesión") }, navigationIcon = {
                IconButton(onClick = onBackToCatalog) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver al catálogo")
                }
            })
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("qariño animal a.c.", fontSize = 11.sp)
                    Image(painterResource(R.drawable.perfiles_brand_mark), "Qariño Animal, labor con amor", Modifier.size(76.dp, 50.dp))
                    Text("labor con amor", fontSize = 10.sp)
                }
                Text("¡Bienvenido!", style = MaterialTheme.typography.titleLarge)
                Text("Tu próxima historia de cariño empieza aquí.")
                OutlinedTextField(
                    value = state.email, onValueChange = onEmailChange, label = { Text("Correo") },
                    enabled = !state.isLoading, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("login_email"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, autoCorrectEnabled = false),
                )
                OutlinedTextField(
                    value = state.password, onValueChange = onPasswordChange, label = { Text("Contraseña") },
                    enabled = !state.isLoading, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("login_password"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = { if (!state.isLoading) submit() }),
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                if (visible) "Ocultar contraseña" else "Mostrar contraseña")
                        }
                    },
                )
                state.error?.let { PerfilesError(it) }
                AppButton("Iniciar sesión", submit, Modifier.fillMaxWidth().testTag("login_submit"),
                    isLoading = state.isLoading, loadingDescription = "Iniciando sesión")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Aún no tienes cuenta?")
                    AppButton("Regístrate", onRegister, variant = ButtonVariant.Clear)
                }
                AppButton("Explorar sin cuenta", onExploreWithoutAccount,
                    Modifier.fillMaxWidth().testTag("login_guest"), variant = ButtonVariant.Outlined)
                SupportingText("Conoce a nuestros peludos sin registrarte.", Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(name = "Login", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Login pequeño", showBackground = true, widthDp = 320, heightDp = 740, fontScale = 1.15f)
@Composable
private fun LoginPreview() {
    AndroidTheme { LoginContent(LoginUiState(), {}, {}, {}, {}, {}, {}) }
}
