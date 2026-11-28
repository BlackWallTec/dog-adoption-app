package com.app.android.presentation.view.viewsVoluntariado

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.designsystem.pages.QConfirmacionInscripcionPage
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
    onVerDetalle: (String) -> Unit = {}, // HU36: id del programa
) {
    val programaConfirmado by viewModel.programaConfirmado.collectAsStateWithLifecycle()
    val programa = programaConfirmado // copia local para el smart cast
    val contexto = LocalContext.current

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

    // El diálogo "¿Cancelar tu inscripción?" ya vive dentro de la página:
    // onCancelEnrollment solo se llama cuando el usuario ya confirmó.
    QConfirmacionInscripcionPage(
        type = programa.tipo,
        title = programa.titulo,
        date = programa.fecha,
        time = programa.horario,
        location = programa.ubicacion,
        capacityText = programa.textoCupo,
        capacityProgress = programa.progresoCupo,
        onBack = onBack,
        onProgramClick = { onVerDetalle(programa.id) }, // HU36
        onCancelEnrollment = { viewModel.cancelarInscripcion(programa.id) },
        onMyVolunteeringClick = onVerMisVoluntariados,
        onExploreProgramsClick = onExplorarProgramas,
        onTabSelected = { onNavSelect(it.ordinal) },
    )
}