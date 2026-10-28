package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.InfoCard
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaMascota
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.SecondaryContainer
import com.app.android.presentation.view.ui.theme.Surface

@Suppress("ktlint:standard:function-naming")
@Composable
fun EnviadaScreen(
    onBackClick: () -> Unit,
    onVerSolicitudesClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 3, // 3 = "Solicitudes
    onNavSelect: (Int) -> Unit = {},
) {
    // Scaffold = el "esqueleto" de una pantalla: arriba, abajo
    // y contenido.
    Scaffold(
        modifier = modifier,
        containerColor = Surface, // fondo de toda la pantalla
        topBar = {
            AppTopBar(
                titulo = "Solicitud de adopción",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
    ) { paddingInterno ->
        // paddingInterno = el espacio que ocupan las barras, para que nada quede tapado
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState()) // permite deslizar si no cabe
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(160.dp)
                        .background(SecondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.MarkEmailRead,
                    contentDescription = null, // decorativo
                    tint = Secondary,
                    modifier = Modifier.size(80.dp),
                )
            }
            Text(
                text = "¡Solicitud enviada!",
                color = InverseSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Te notificaremos cuando haya novedades",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            TarjetaMascota(
                foto = painterResource(id = R.drawable.perro_prueba),
                nombre = "Bruno",
                edad = "2 años",
                sexo = "Macho",
                tamano = "Mediano",
                raza = "Mestizo",
                ciudad = "Querétaro",
            )
            Text(
                text = "Solicitud QA-0248 · En revisión",
                color = InverseSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            InfoCard(
                // estos luego los remplazamos por cosas como $nombreSolicitante, $nombreMascota y $correo
                descripcion = "X, revisaremos tu solicitud para Y y te contactaremos en Z.",
                icono = Icons.Outlined.Mail,
            )
            AppButton(
                text = "Ver mis solicitudes",
                onClick = onVerSolicitudesClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
            )
            AppButton(
                text = "Volver al catálogo",
                onClick = onBackClick,
                variant = ButtonVariant.Outlined,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            )
        }
    }
}

@Preview
@Composable
fun EnviadaScreenPreview() {
    AndroidTheme {
        EnviadaScreen(
            onBackClick = {},
            onVerSolicitudesClick = {},
        )
    }
}
