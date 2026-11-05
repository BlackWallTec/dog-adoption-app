package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
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
            ) {
                TextButton(
                    onClick = onBackToCatalog,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.inverseSurface),
                ) {
                    Text("Volver al catálogo", fontSize = 12.sp)
                }

                BrandMark(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
                Text(
                    "¡Bienvenido!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.inverseSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tu próxima historia de cariño empieza aquí.",
                    fontSize = 14.sp,
                    color = colors.inverseSurface,
                )
                Spacer(Modifier.height(20.dp))

                Text("Correo", fontSize = 14.sp, color = colors.inverseSurface)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth().testTag("login_email").semantics { contentDescription = "Correo" },
                    enabled = !state.isLoading,
                    singleLine = true,
                    placeholder = { Text("tu@correo.com", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = colors.secondary)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                    isError = state.emailError != null || state.credentialsRejected,
                    supportingText = state.emailError?.let { message -> { Text(message) } },
                    shape = RoundedCornerShape(16.dp),
                    colors = loginFieldColors(),
                )
                Spacer(Modifier.height(16.dp))

                Text("Contraseña", fontSize = 14.sp, color = colors.inverseSurface)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth().testTag("login_password").semantics { contentDescription = "Contraseña" },
                    enabled = !state.isLoading,
                    singleLine = true,
                    placeholder = { Text("Escribe tu contraseña", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.secondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = colors.secondary,
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (!state.isLoading) submit() }),
                    isError = state.passwordError != null || state.credentialsRejected,
                    supportingText = state.passwordError?.let { message -> { Text(message) } },
                    shape = RoundedCornerShape(16.dp),
                    colors = loginFieldColors(),
                )

                Spacer(Modifier.height(18.dp))

                state.error?.let { message ->
                    Surface(
                        color = colors.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    ) {
                        Text(
                            message,
                            color = colors.onErrorContainer,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
                if (state.isSignedIn) {
                    Text(
                        "Sesión iniciada correctamente.",
                        color = colors.secondary,
                        modifier = Modifier.padding(bottom = 12.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }

                Button(
                    onClick = { submit() },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("login_submit"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.inverseSurface,
                    ),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.inverseSurface,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(10.dp))
                    }
                    Text(
                        if (state.isLoading) "Iniciando sesión…" else "Iniciar sesión",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("¿Aún no tienes cuenta?", fontSize = 14.sp, color = colors.inverseSurface)
                    TextButton(onClick = onRegister, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("Regístrate", color = colors.secondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    onClick = onExploreWithoutAccount,
                    modifier = Modifier.fillMaxWidth().testTag("login_guest"),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Explorar sin cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.inverseSurface)
                        Spacer(Modifier.height(2.dp))
                        Text("Conoce a nuestros peludos sin registrarte.", fontSize = 13.sp, textAlign = TextAlign.Center, color = colors.inverseSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
    cursorColor = MaterialTheme.colorScheme.secondary,
)

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
