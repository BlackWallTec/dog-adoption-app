package com.app.android.presentation.view.viewsPerfiles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.CorreoUiState
import com.app.android.presentation.viewmodel.viewModelPerfiles.CorreoViewModel

@Composable
fun CorreoScreen(onCancel: () -> Unit, onFinished: (Boolean) -> Unit, viewModel: CorreoViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cancelar = { viewModel.cancelar(); onCancel() }
    BackHandler(onBack = cancelar)
    LaunchedEffect(state.completado) {
        if (state.completado) { viewModel.consumirExito(); onFinished(state.recuperar) }
    }
    CorreoContent(state, viewModel::correo, viewModel::codigo, viewModel::contrasena,
        viewModel::repetida, viewModel::solicitar, viewModel::confirmar, cancelar)
}

// La vista recibe estado y eventos; el envío y la validación viven fuera de Compose.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CorreoContent(state: CorreoUiState, onCorreo: (String) -> Unit, onCodigo: (String) -> Unit,
    onPassword: (String) -> Unit, onRepetida: (String) -> Unit, onSolicitar: () -> Unit,
    onConfirmar: () -> Unit, onCancel: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(if (state.recuperar) "Recuperar contraseña" else "Confirmar correo") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (state.recuperar) "Solicita un código para elegir una nueva contraseña." else "Escribe el código de seis dígitos que recibiste. Si no llegó, solicita otro.")
            OutlinedTextField(state.correo, onCorreo, label = { Text("Correo") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth().testTag("correo_email"))
            if (state.codigoSolicitado) {
                OutlinedTextField(state.codigo, onCodigo, label = { Text("Código de seis dígitos") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !state.cargando,
                    modifier = Modifier.fillMaxWidth().testTag("correo_codigo"))
                if (state.recuperar) {
                    OutlinedTextField(state.contrasena, onPassword, label = { Text("Nueva contraseña") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !state.cargando, modifier = Modifier.fillMaxWidth().testTag("correo_password"))
                    OutlinedTextField(state.repetida, onRepetida, label = { Text("Repetir contraseña") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !state.cargando, modifier = Modifier.fillMaxWidth().testTag("correo_repetida"))
                }
                AppButton(if (state.recuperar) "Cambiar contraseña" else "Confirmar correo", onConfirmar,
                    Modifier.fillMaxWidth().testTag("correo_confirmar"), isLoading = state.cargando)
            }
            state.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            state.error?.let { PerfilesError(it) }
            AppButton(if (state.codigoSolicitado) "Solicitar otro código" else "Enviar código", onSolicitar,
                Modifier.fillMaxWidth().testTag("correo_enviar"), enabled = !state.cargando,
                variant = ButtonVariant.Outlined)
            // Cancelar siempre está disponible y cancela el trabajo pendiente del ViewModel.
            AppButton("Cancelar y volver al login", onCancel, Modifier.fillMaxWidth(), variant = ButtonVariant.Clear)
        }
    }
}

@Preview(name = "Confirmar correo", showBackground = true)
@Composable
private fun CorreoPreview() {
    AndroidTheme { CorreoContent(CorreoUiState(correo = "tu@correo.com", codigoSolicitado = true), {}, {}, {}, {}, {}, {}, {}) }
}
