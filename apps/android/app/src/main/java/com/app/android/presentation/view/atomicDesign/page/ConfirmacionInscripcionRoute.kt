package com.app.android.presentation.view.viewsVoluntariado

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.organism.ConfirmActionDialog
import com.app.android.presentation.view.atomicDesign.page.VolunteeringConfirmationScreen
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel

// Conecta la confirmación con el ViewModel COMPARTIDO del catálogo.
// No dibuja nada por sí misma: lee el programa recién inscrito y llena la página.
@Suppress("ktlint:standard:function-naming")
@Composable
fun ConfirmacionInscripcionRoute(
    viewModel: ProgramasViewModel,
    onBack: () -> Unit,
    onExplorarProgramas: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    onVerMisVoluntariados: () -> Unit = {}, // HU40
) {
    val programaConfirmado by viewModel.programaConfirmado.collectAsStateWithLifecycle()
    val programa = programaConfirmado // copia local para el smart cast
    val contexto = LocalContext.current
    var confirmandoCancelacion by remember { mutableStateOf(false) }

    // HU37: esta pantalla no tiene snackbar, así que los avisos (éxito o error al cancelar) salen como Toast
    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
    }
    // Si la cancelación salió bien, se regresa a la lista de programas
    LaunchedEffect(Unit) {
        viewModel.cancelacionExitosa.collect { onExplorarProgramas() }
    }

    // Sin programa no hay nada que confirmar (por ejemplo, si Android cerró la app en segundo plano)
    if (programa == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    VolunteeringConfirmationScreen(
        tipoBadge = programa.tipo,
        tituloPrograma = programa.titulo,
        fecha = programa.fecha,
        horario = programa.horario,
        ubicacion = programa.ubicacion,
        textoCupo = programa.textoCupo,
        progresoCupo = programa.progresoCupo,
        onBackClick = onBack,
        onExplorarProgramasClick = onExplorarProgramas,
        onNavSelect = onNavSelect,
        onVerMisProgramasClick = onVerMisVoluntariados,
        onCancelarInscripcionClick = { confirmandoCancelacion = true },
    )

    // Confirmación antes de cancelar: la acción libera el lugar y otra persona puede tomarlo
    if (confirmandoCancelacion) {
        ConfirmActionDialog(
            titulo = "¿Cancelar tu inscripción?",
            descripcion = "Si cancelas, liberarás tu lugar en ${programa.titulo}. ¿Quieres continuar?",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {
                viewModel.cancelarInscripcion(programa.id)
                confirmandoCancelacion = false
            },
            onDismiss = { confirmandoCancelacion = false },
        )
    }
}