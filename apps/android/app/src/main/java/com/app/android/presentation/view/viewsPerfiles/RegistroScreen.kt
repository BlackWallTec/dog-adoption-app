package com.app.android.presentation.view.viewsPerfiles

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesPerfiles.validarRegistro
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.RegistroUiState
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    state: RegistroUiState, onChange: (String, String) -> Unit,
    onPrivacyChange: (Boolean) -> Unit, onRegister: () -> Unit, onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var visible by remember { mutableStateOf(false) }
    var noticeVisible by remember { mutableStateOf(false) }
    val fields = listOf(
        Triple("nombre", "Nombre(s)", state.datos.nombre), Triple("apellidos", "Apellidos", state.datos.apellidos),
        Triple("email", "Correo", state.datos.email), Triple("telefono", "Teléfono", state.datos.telefono),
        Triple("fechaNacimiento", "Fecha de nacimiento", state.datos.fechaNacimiento),
        Triple("calle", "Dirección", state.datos.calle), Triple("numeroExterior", "Número exterior", state.datos.numeroExterior),
        Triple("codigoPostal", "Código postal", state.datos.codigoPostal),
        Triple("passwd", "Contraseña", state.datos.passwd), Triple("confirmPasswd", "Confirmar contraseña", state.datos.confirmPasswd),
    )
    val requesters = remember { (fields.map { it.first } + "privacidad").associateWith { FocusRequester() } }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopAppBar(title = { Text("Crear cuenta") }, actions = {
            TextButton(onClick = onCancel, modifier = Modifier.testTag("register_cancel")) { Text("Cancelar") }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Tu próxima historia de cariño empieza aquí.", style = MaterialTheme.typography.titleLarge)
            Text("Completa tus datos. Todos los campos son obligatorios.", style = MaterialTheme.typography.bodyMedium)
            fields.forEach { (name, label, value) ->
                val password = name in listOf("passwd", "confirmPasswd")
                val hint = when (name) {
                    "email" -> "Gmail, Outlook, Yahoo, Hotmail o iCloud."
                    "fechaNacimiento" -> "AAAA-MM-DD · Debes tener 18 años o más."
                    "calle" -> "Incluye calle, colonia, municipio y estado."
                    "passwd" -> "Usa al menos 8 caracteres."
                    else -> null
                }
                val keyboard = when (name) {
                    "email" -> KeyboardType.Email
                    "telefono" -> KeyboardType.Phone
                    "numeroExterior", "codigoPostal" -> KeyboardType.Number
                    "passwd", "confirmPasswd" -> KeyboardType.Password
                    else -> KeyboardType.Text
                }
                OutlinedTextField(
                    value = value, onValueChange = { onChange(name, it) },
                    label = { Text(label) }, enabled = !state.isLoading, singleLine = true,
                    isError = state.campos[name] != null,
                    supportingText = (state.campos[name] ?: hint)?.let { message -> { Text(message) } },
                    modifier = Modifier.fillMaxWidth().testTag("register_$name").focusRequester(requesters.getValue(name)),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = ImeAction.Next, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = if (name == "passwd") {{ IconButton(onClick = { visible = !visible }, enabled = !state.isLoading) {
                        Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (visible) "Ocultar contraseñas" else "Mostrar contraseñas")
                    } }} else if (name == "fechaNacimiento") {{ IconButton(onClick = {
                        focus.clearFocus()
                        val date = Calendar.getInstance()
                        val pieces = state.datos.fechaNacimiento.split('-').mapNotNull { it.toIntOrNull() }
                        DatePickerDialog(context, { _, year, month, day -> onChange(name, String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day)) },
                            pieces.getOrNull(0)?.takeIf { it in 1900..date.get(Calendar.YEAR) } ?: 2000,
                            (pieces.getOrNull(1)?.takeIf { it in 1..12 } ?: 1) - 1,
                            pieces.getOrNull(2)?.takeIf { it in 1..31 } ?: 1)
                            .apply { datePicker.maxDate = date.timeInMillis }.show()
                    }, enabled = !state.isLoading) { Icon(Icons.Outlined.DateRange, "Elegir fecha de nacimiento") } }} else null,
                )
            }
            TextButton(onClick = { noticeVisible = !noticeVisible }) { Text("Aviso de privacidad del registro") }
            if (noticeVisible) Text("Tus datos se utilizarán para crear tu cuenta, permitirte acceder a ella y gestionar tus trámites con Qariño Animal. El personal autorizado podrá consultar la información necesaria para atenderlos.")
            Row(Modifier.fillMaxWidth().testTag("register_privacidad").focusRequester(requesters.getValue("privacidad"))
                .toggleable(state.datos.privacidad, enabled = !state.isLoading, role = Role.Checkbox, onValueChange = onPrivacyChange), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.datos.privacidad, onCheckedChange = null, colors = CheckboxDefaults.colors(
                    uncheckedColor = if (state.campos["privacidad"] != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                ))
                Text("He leído y acepto el aviso de privacidad.", Modifier.padding(start = 8.dp))
            }
            state.campos["privacidad"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.error?.let { PerfilesError(it) }
            AppButton("Crear mi cuenta", {
                focus.clearFocus()
                onRegister()
                validarRegistro(state.datos).keys.firstOrNull()?.let { requesters[it]?.requestFocus() }
            }, Modifier.fillMaxWidth().testTag("register_submit"), isLoading = state.isLoading)
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Ya tengo cuenta. Iniciar sesión") }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun PerfilesError(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Preview(name = "Crear cuenta", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Crear cuenta pequeña", showBackground = true, widthDp = 320, heightDp = 740, fontScale = 1.15f)
@Composable
private fun RegistroPreview() { AndroidTheme { RegistroScreen(RegistroUiState(), { _, _ -> }, {}, {}, {}) } }
