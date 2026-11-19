package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.Usuario
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.ListAccess
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.PrimaryContainer
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    state: SesionUiState,
    onRetry: () -> Unit,
    onCatalog: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    onMisSolicitudes: () -> Unit = {},
    onMisProgramas: () -> Unit = {},
    onDatosFiscales: () -> Unit = {},
) {
    // Mientras se revisa la sesión, todavía no muestro datos de la cuenta.
    if (state.comprobando) {
        InicioSesionScreen(state, onRetry, onCatalog)
        return
    }

    val sesion = state.sesion
    // Si ya no hay sesión, ofrezco entrar otra vez sin mostrar datos anteriores.
    if (sesion == null) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Inicia sesión para consultar tu cuenta.", style = MaterialTheme.typography.titleLarge)
            state.error?.let { PerfilesError(it) }
            AppButton("Iniciar sesión", onLogin, Modifier.fillMaxWidth())
            TextButton(onClick = onCatalog) { Text("Volver al catálogo") }
        }
        return
    }

    // Diseño de Figma "Configuración · Opciones": solo con las pantallas que ya existen.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = { NavBar(selected = 4, onSelect = onNavSelect) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Configuración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Administra tu cuenta y tus preferencias.", style = MaterialTheme.typography.bodyMedium)

            TituloSeccion("Mi perfil")
            if (state.cargandoPerfil) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.perfil?.let { p ->
                val nombre = "${p.nombre} ${p.apellidos}".trim()
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(72.dp).background(PrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(iniciales(p.nombre, p.apellidos), style = MaterialTheme.typography.headlineSmall)
                    }
                    Text(nombre.ifBlank { "Sin datos" }, Modifier.padding(top = 8.dp).testTag("account_name"), style = MaterialTheme.typography.titleLarge)
                    Text("Cuenta ${sesion.usuario.rol}", style = MaterialTheme.typography.bodyMedium)
                }
                DatoCuenta("Teléfono", p.telefono)
                DatoCuenta("Fecha de nacimiento", p.fechaNacimiento.take(10).split('-').reversed().joinToString("/"))
                DatoCuenta("Dirección", listOf(p.calle, p.numeroExterior, p.codigoPostal.takeIf { it.isNotBlank() }?.let { "C. P. $it" }.orEmpty()).filter { it.isNotBlank() }.joinToString(", "))
            }
            // Evito otra consulta mientras ya se está cargando o cerrando la sesión.
            state.error?.let {
                PerfilesError(it)
                AppButton(
                    "Volver a consultar", onRetry, Modifier.fillMaxWidth(),
                    enabled = !state.cargandoPerfil && !state.cerrando,
                    variant = ButtonVariant.Outlined,
                )
            }
            ListAccess("Mis solicitudes", onMisSolicitudes, icon = Icons.AutoMirrored.Outlined.Assignment)
            ListAccess("Mis programas", onMisProgramas, icon = Icons.Outlined.VolunteerActivism)

            TituloSeccion("Tu cuenta")
            ListAccess(
                "Datos fiscales", onDatosFiscales, Modifier.testTag("account_datos_fiscales"),
                subtitle = "Información para tus CFDI",
                icon = Icons.Outlined.Description,
            )

            AppButton("Cerrar sesión", onLogout, Modifier.fillMaxWidth().padding(top = 16.dp).testTag("account_logout"), variant = ButtonVariant.Outlined, isLoading = state.cerrando)
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(texto, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
}

// "Sofía García" -> "SG"
private fun iniciales(nombre: String, apellidos: String): String =
    listOf(nombre, apellidos).mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }.joinToString("")

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

@Preview(name = "Configuración", showBackground = true, widthDp = 360, heightDp = 1200)
@Composable
private fun CuentaPreview() {
    // Estos datos son solo para ver el diseño; no hacen una llamada al servidor.
    val sesion = Sesion(
        usuario = Usuario(id = "preview", rol = "Cliente"),
        token = "",
    )
    AndroidTheme {
        CuentaScreen(
            state = SesionUiState(
                sesion = sesion,
                comprobando = false,
                perfil = Perfil(nombre = "Sofía", apellidos = "García López", telefono = "4421234567"),
            ),
            onRetry = {}, onCatalog = {}, onLogout = {}, onLogin = {},
        )
    }
}
