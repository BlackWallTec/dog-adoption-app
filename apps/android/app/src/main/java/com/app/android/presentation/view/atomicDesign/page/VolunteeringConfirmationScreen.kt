package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.ConfirmationHeader
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.ConfirmationActionsGroup
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.TarjetaVoluntariado
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.ui.theme.Surface

@Composable
fun VolunteeringConfirmationScreen(
    tipoBadge: String,
    tituloPrograma: String,
    fecha: String,
    horario: String,
    ubicacion: String,
    textoCupo: String,
    progresoCupo: Float,
    correoAviso: String,
    onBackClick: () -> Unit,
    onVerMisProgramasClick: () -> Unit,
    onExplorarProgramasClick: () -> Unit,
    onCancelarInscripcionClick: () -> Unit,
    onVerDetalleProgramaClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 1, // 1 = "Ayudar", la pestaña de Voluntariado
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                titulo = "Voluntariado",
                onBackClick = {},
                modifier = Modifier.background(Surface),
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            ConfirmationHeader(
                titulo = "¡Te inscribiste!",
                subtitulo = "Gracias por sumar tu cariño. Tu lugar está confirmado.",
                modifier = Modifier.padding(top = 24.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TarjetaVoluntariado(
                     tipo = tipoBadge,
                     titulo = tituloPrograma,
                     fecha = fecha,
                     horario = horario,
                     ubicacion = ubicacion,
                     textoCupo = textoCupo,
                     progresoCupo = progresoCupo,
                     alVerDetalle = onVerDetalleProgramaClick,
                     alAccionBoton = onCancelarInscripcionClick,
                     textoBoton = "Cancelar inscripción",
                     varianteBoton = ButtonVariant.Danger,
                )
            }

            InfoMessageCard(mensaje = correoAviso)

            ConfirmationActionsGroup(
                onVerMisProgramasClick = onVerMisProgramasClick,
                onExplorarProgramasClick = onExplorarProgramasClick,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Placeholders temporales — bórralos en cuanto conectes tus componentes reales
// (TopBar real y TarjetaVoluntariado). Quedan aquí solo para que el Preview
// compile y se vea completo mientras tanto.
// ---------------------------------------------------------------------------



@Preview(showBackground = true)
@Composable
private fun VolunteeringConfirmationScreenPreview() {
    AndroidTheme {
        VolunteeringConfirmationScreen(
            tipoBadge = "Inscrito",
            tituloPrograma = "Paseos con cariño",
            fecha = "Sáb. 10 oct. 2026",
            horario = "09:00 – 11:00 h",
            ubicacion = "Refugio Qariño · Querétaro",
            textoCupo = "8/15 lugares ocupados · 7 disponibles",
            progresoCupo = 8f / 15f,
            correoAviso = "Enviamos los detalles a sofia.garcia@correo.com. Recuerda llegar 10 minutos antes.",
            onBackClick = {},
            onVerMisProgramasClick = {},
            onExplorarProgramasClick = {},
            onCancelarInscripcionClick = {},
        )
    }
}

