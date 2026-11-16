package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.SupportingText
import com.app.android.presentation.view.atomicDesign.molecule.FormField
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.LoginUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.validateLogin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBackToCatalog: () -> Unit,
    onExploreWithoutAccount: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val submit = {
        focusManager.clearFocus()
        onLogin()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Iniciar sesión", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                    navigationIcon = {
                        IconButton(onClick = onBackToCatalog) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Volver al catálogo",
                                tint = colors.secondary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface,
                        titleContentColor = colors.inverseSurface,
                    ),
                )
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding).imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BrandMark(modifier = Modifier.align(Alignment.CenterHorizontally))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "¡Bienvenido!",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.inverseSurface,
                    )
                    Text(
                        "Tu próxima historia de cariño empieza aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inverseSurface,
                    )
                }

                FormField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = "Correo",
                    enabled = !state.isLoading,
                    isError = state.emailError != null || state.credentialsRejected,
                    errorText = state.emailError,
                    fieldModifier = Modifier.testTag("login_email"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                )

                FormField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    enabled = !state.isLoading,
                    isError = state.passwordError != null || state.credentialsRejected,
                    errorText = state.passwordError,
                    fieldModifier = Modifier.testTag("login_password"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (!state.isLoading) submit() }),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = colors.secondary,
                            )
                        }
                    },
                )

                state.error?.let { PerfilesError(it) }
                if (state.isSignedIn) {
                    Text(
                        "Sesión iniciada correctamente.",
                        color = colors.secondary,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }

                AppButton(
                    text = "Iniciar sesión",
                    onClick = { submit() },
                    modifier = Modifier.fillMaxWidth().testTag("login_submit"),
                    isLoading = state.isLoading,
                    loadingDescription = "Iniciando sesión",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("¿Aún no tienes cuenta?", style = MaterialTheme.typography.bodyMedium, color = colors.inverseSurface)
                    AppButton(text = "Regístrate", onClick = onRegister, variant = ButtonVariant.Clear)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AppButton(
                        text = "Explorar sin cuenta",
                        onClick = onExploreWithoutAccount,
                        modifier = Modifier.fillMaxWidth().testTag("login_guest"),
                        variant = ButtonVariant.Outlined,
                    )
                    Spacer(Modifier.height(6.dp))
                    SupportingText(
                        text = "Conoce a nuestros peludos sin registrarte.",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("qariño animal a.c.", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.inverseSurface)
        Image(
            painter = painterResource(R.drawable.perfiles_brand_mark),
            contentDescription = "Qariño Animal, labor con amor",
            modifier = Modifier.size(width = 76.dp, height = 50.dp),
        )
        Text("labor con amor", fontSize = 10.sp, color = MaterialTheme.colorScheme.inverseSurface)
    }
}

@Preview(name = "Login móvil", showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Preview(name = "Login móvil · pantalla pequeña", showBackground = true, widthDp = 320, heightDp = 740, fontScale = 1.15f)
@Composable
private fun LoginScreenPreview() {
    var state by remember { mutableStateOf(LoginUiState()) }
    AndroidTheme {
        LoginScreen(
            state = state,
            onEmailChange = { state = state.copy(email = it, emailError = null) },
            onPasswordChange = { state = state.copy(password = it, passwordError = null) },
            onLogin = { state = validateLogin(state) },
            onBackToCatalog = {},
            onExploreWithoutAccount = {},
            onRegister = {},
        )
    }
}

@Preview(name = "Login móvil · credenciales incorrectas", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginErrorPreview() {
    AndroidTheme {
        LoginScreen(
            state = LoginUiState(email = "ana@correo.com", error = "Correo o contraseña incorrectos.", credentialsRejected = true),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = {},
            onBackToCatalog = {},
            onExploreWithoutAccount = {},
            onRegister = {},
        )
    }
}