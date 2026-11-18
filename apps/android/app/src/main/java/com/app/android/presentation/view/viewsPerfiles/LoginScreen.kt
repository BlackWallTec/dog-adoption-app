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

// Pantalla del login

@Composable
// Sólo dibuja, recibe estado y lambdas
fun LoginScreen(
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
    onAuthenticated: (Sesion) -> Unit,
    // ViewModel por default
    viewModel: LoginViewModel = hiltViewModel(),
) {

    // Apegándose al ciclo de vida de la pantalla recoge uiState como un State observable
    // para el motor, pausa cuando la pantalla no está activa
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Ahora se recolecta todo cambio del uiState de ViewModel

    // Con LaunchedEffect, sólo una recomposición
    // referente astate.sesionLista vuelve a lanzar el bloque
    LaunchedEffect(state.sesionLista) {
        // El bloque sólo ejecuta si state.sesionLista no es null
        state.sesionLista?.let{ sesion ->
            // Consume la sesión
            viewModel.consumirSesion()
            // y navega a la pantalla de inicio
            onAuthenticated(sesion)
        }
    }

    // Define el comportamiento a seguir al pulsar el botón de atrás de la system navbar
    BackHandler {
        viewModel.cancelLogin();
        onBackToCatalog()
    }

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
    state: LoginUiState,    // Datos y estados de la pantalla
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
) {
    // Obtiene el control para quitar el foco de los campos que obtienen el foco
    // (por ejemplo al ser tocados, gracias a esto se puede controlar ese foco)
    val focus = LocalFocusManager.current

    // Controla la visibilidad de la contraseña
    var visible by rememberSaveable {
        mutableStateOf(false)
    }
    
    // Quita el foco para llamar a onLogin
    val submit = {
        focus.clearFocus();
        onLogin()
    }

    // Contenido de la pantalla
    // Se utiliza trailing lambda para pasar al parámetro content una función para dibujar
    // el contenido. Básicamente la notación mencionada equivale a poner toda la función de padding
    // (el content) entre los paréntesis del Scaffold, para que este la use con algún paddingValue
    // Ver documentación de Scaffold para más info.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(title = { Text("Iniciar sesión") }, navigationIcon = {
                IconButton(onClick = onBackToCatalog) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Volver al catálogo")
                }
            }
            )
        },
    ) {
        // El paddingValues que especifique Scaffold se utilizará como padding en esta función
        padding ->
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

// * Me refiero al runtime de Compose como el motor
// porque observa estado y decide qué se recompone y lo dibuja