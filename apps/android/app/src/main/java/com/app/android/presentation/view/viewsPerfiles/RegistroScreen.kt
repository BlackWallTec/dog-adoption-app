package com.app.android.presentation.view.viewsPerfiles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroViewModel

@Composable
fun RegistroScreen(
    onCancel: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.registroExitoso) {
        if (state.registroExitoso) { viewModel.consumirRegistro(); onRegistered() }
    }
    BackHandler { viewModel.cancelar(); onCancel() }
    RegistroContent(state, viewModel::actualizar, viewModel::privacidad, viewModel::registrar,
        onCancel = { viewModel.cancelar(); onCancel() },
        onColonia = viewModel::seleccionarColonia)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegistroContent(
    state: RegistroUiState,
    onChange: (String, String) -> Unit,
    onPrivacyChange: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onCancel: () -> Unit,
    onColonia: (String) -> Unit,
) {
    val focus = LocalFocusManager.current
    var visible by rememberSaveable { mutableStateOf(false) }
    var avisoVisible by rememberSaveable { mutableStateOf(false) }
    val d = state.datos
    // Las claves son las de fields del backend; los valores provienen del modelo de dominio.
    val campos = listOf(
        Triple("nombre", "Nombre(s)", d.nombre), Triple("apellidos", "Apellidos", d.apellidos),
        Triple("correo", "Correo", d.correo), Triple("telefono", "Teléfono", d.telefono),
        Triple("fecha_nacimiento", "Fecha de nacimiento", d.fechaNacimiento),
        Triple("calle", "Calle", d.calle), Triple("numero_exterior", "Número exterior", d.numeroExterior),
        Triple("numero_interior", "Número interior (opcional)", d.numeroInterior),
        Triple("codigoPostal", "Código postal", state.codigoPostal),
        Triple("contrasena", "Contraseña", d.contrasena), Triple("confirmContrasena", "Confirmar contraseña", d.confirmContrasena),
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopAppBar(title = { Text("Crear cuenta") }, actions = {
            TextButton(onClick = onCancel, modifier = Modifier.testTag("register_cancel")) { Text("Cancelar") }
        }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Tu próxima historia de cariño empieza aquí.", style = MaterialTheme.typography.titleLarge)
            Text("Completa tus datos. El número interior es opcional.")
            campos.forEach { (clave, etiqueta, valor) ->
                if (clave == "fecha_nacimiento") {
                    FechaNacimientoField(valor, { onChange(clave, it) }, !state.isLoading, state.campos[clave])
                } else {
                    val password = clave == "contrasena" || clave == "confirmContrasena"
                    val teclado = when (clave) {
                        "correo" -> KeyboardType.Email
                        "telefono" -> KeyboardType.Phone
                        "codigoPostal", "numero_exterior", "numero_interior" -> KeyboardType.Number
                        else -> if (password) KeyboardType.Password else KeyboardType.Text
                    }
                    OutlinedTextField(
                        value = valor, onValueChange = { onChange(clave, it) }, label = { Text(etiqueta) },
                        enabled = !state.isLoading, singleLine = true, isError = state.campos[clave] != null,
                        supportingText = state.campos[clave]?.let { mensaje -> { Text(mensaje) } },
                        modifier = Modifier.fillMaxWidth().testTag("register_$clave"), shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = ImeAction.Next, autoCorrectEnabled = false),
                        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                        trailingIcon = if (clave == "contrasena") {{
                            IconButton(onClick = { visible = !visible }) {
                                Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    if (visible) "Ocultar contraseñas" else "Mostrar contraseñas")
                            }
                        }} else null,
                    )
                }
                if (clave == "codigoPostal") SelectorColonia(
                    state.codigoPostal, state.colonias, d.idCodigoPostal, state.cargandoColonias,
                    state.errorColonias ?: state.campos["id_codigo_postal"], !state.isLoading, onColonia,
                )
            }
            TextButton(onClick = { avisoVisible = !avisoVisible }) { Text("Aviso de privacidad del registro") }
            if (avisoVisible) Text("Tus datos se utilizarán para crear tu cuenta, permitirte acceder a ella y gestionar tus trámites con Qariño Animal. El personal autorizado podrá consultar la información necesaria para atenderlos.")
            Row(Modifier.fillMaxWidth().testTag("register_privacidad")
                .toggleable(d.privacidad, enabled = !state.isLoading, role = Role.Checkbox, onValueChange = onPrivacyChange),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = d.privacidad, onCheckedChange = null)
                Text("He leído y acepto el aviso de privacidad.", Modifier.padding(start = 8.dp))
            }
            state.campos["privacidad"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.error?.let { PerfilesError(it) }
            AppButton("Crear mi cuenta", { focus.clearFocus(); onRegister() },
                Modifier.fillMaxWidth().testTag("register_submit"), isLoading = state.isLoading)
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Ya tengo cuenta. Iniciar sesión")
            }
        }
    }
}

@Preview(name = "Crear cuenta", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RegistroPreview() {
    AndroidTheme { RegistroContent(RegistroUiState(), { _, _ -> }, {}, {}, {}, {}) }
}
