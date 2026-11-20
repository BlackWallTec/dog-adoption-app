package com.app.android.presentation.view.viewsConfiguracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.molecule.ListAccess
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme

// Menú de Ajustes como en Figma ("Configuración · Opciones"): lleva a las pantallas de cada módulo.
// Solo tiene los renglones cuya pantalla ya existe.
@Composable
fun ConfiguracionScreen(
    onNavSelect: (Int) -> Unit,
    onMiCuenta: () -> Unit,
    onMisSolicitudes: () -> Unit,
    onMisProgramas: () -> Unit,
    onDatosFiscales: () -> Unit,
) {
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
            ListAccess("Mi cuenta", onMiCuenta, subtitle = "Tus datos de contacto", icon = Icons.Outlined.Person)
            ListAccess("Mis solicitudes", onMisSolicitudes, icon = Icons.AutoMirrored.Outlined.Assignment)
            ListAccess("Mis programas", onMisProgramas, icon = Icons.Outlined.VolunteerActivism)

            TituloSeccion("Tu cuenta")
            ListAccess(
                "Datos fiscales", onDatosFiscales, Modifier.testTag("config_datos_fiscales"),
                subtitle = "Información para tus CFDI",
                icon = Icons.Outlined.Description,
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(texto, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
}

@Preview(name = "Configuración", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ConfiguracionPreview() {
    AndroidTheme {
        ConfiguracionScreen(onNavSelect = {}, onMiCuenta = {}, onMisSolicitudes = {}, onMisProgramas = {}, onDatosFiscales = {})
    }
}
