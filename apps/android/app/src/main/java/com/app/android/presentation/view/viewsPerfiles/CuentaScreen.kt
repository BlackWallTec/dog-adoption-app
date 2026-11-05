package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(state: SesionUiState, onRetry: () -> Unit, onCatalog: () -> Unit, onLogout: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.surface, topBar = {
        TopAppBar(title = { Text("Mi cuenta") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("¡Qué gusto tenerte aquí!", style = MaterialTheme.typography.headlineSmall)
            DatoCuenta("Correo", state.sesion?.usuario?.email.orEmpty(), "account_email")
            DatoCuenta("Tipo de cuenta", state.sesion?.usuario?.rol.orEmpty())
            HorizontalDivider()
            Text("Mis datos", style = MaterialTheme.typography.titleLarge)
            if (state.cargandoPerfil) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.perfil?.let { p ->
                DatoCuenta("Nombre", "${p.nombre} ${p.apellidos}".trim(), "account_name")
                DatoCuenta("Teléfono", p.telefono)
                DatoCuenta("Fecha de nacimiento", p.fechaNacimiento.split('-').reversed().joinToString("/"))
                DatoCuenta("Dirección", listOf(p.calle, p.numeroExterior, p.codigoPostal.takeIf { it.isNotBlank() }?.let { "C. P. $it" }.orEmpty()).filter { it.isNotBlank() }.joinToString(", "))
            }
            state.error?.let { PerfilesError(it); AppButton("Volver a consultar", onRetry, Modifier.fillMaxWidth(), variant = ButtonVariant.Outlined) }
            AppButton("Ver catálogo", onCatalog, Modifier.fillMaxWidth(), enabled = !state.cerrando)
            AppButton("Cerrar sesión", onLogout, Modifier.fillMaxWidth().testTag("account_logout"), variant = ButtonVariant.Outlined, isLoading = state.cerrando)
        }
    }
}

@Composable
private fun DatoCuenta(label: String, value: String, tag: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value.ifBlank { "Sin datos" }, Modifier.testTag(tag))
    }
}

@Composable
fun InicioSesionScreen(state: SesionUiState, onRetry: () -> Unit, onGuest: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.comprobando) { CircularProgressIndicator(); Text("Comprobando tu sesión…", Modifier.padding(top = 16.dp)) }
            else {
                state.error?.let { PerfilesError(it) }
                AppButton("Reintentar", onRetry, Modifier.fillMaxWidth().padding(top = 16.dp))
                TextButton(onClick = onGuest) { Text("Explorar sin cuenta") }
            }
        }
    }
}

@Preview(name = "Mi cuenta", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CuentaPreview() { AndroidTheme { CuentaScreen(SesionUiState(comprobando = false, cargandoPerfil = true), {}, {}, {}) } }
