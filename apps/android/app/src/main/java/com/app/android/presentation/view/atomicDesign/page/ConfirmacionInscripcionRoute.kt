package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
) {
    val programaConfirmado by viewModel.programaConfirmado.collectAsStateWithLifecycle()
    val programa = programaConfirmado // copia local para el smart cast

    // Sin programa no hay nada que confirmar (por ejemplo, si Android cerró la app en segundo plano)
    if (programa == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    VolunteeringConfirmationScreen(
        tipoBadge = programa.tipo,
        tituloPrograma = programa.nombre,
        fecha = programa.fechaTexto(),
        horario = programa.horarioTexto(),
        ubicacion = programa.ubicacionTexto(),
        textoCupo = "",
        progresoCupo = 0f,
        onBackClick = onBack,
        onExplorarProgramasClick = onExplorarProgramas,
        onNavSelect = onNavSelect,
        onVerMisProgramasClick = { /* no hace nada */ },
        onCancelarInscripcionClick = { /* no hace nada */ },
    )
}