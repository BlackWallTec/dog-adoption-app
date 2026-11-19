package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.organism.ConfirmActionDialog
import com.app.android.presentation.view.atomicDesign.page.ProgramasPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.MisProgramasViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState

@Suppress("ktlint:standard:function-naming")
@Composable
fun MisProgramasScreen(
    viewModel: MisProgramasViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {},
    onVerDetalle: ((String) -> Unit)? = null, // HU36: id del programa
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var programaPorCancelar by remember { mutableStateOf<ProgramaUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }

    ProgramasPage(
        uiState = uiState,
        onReintentar = viewModel::cargarProgramas,
        onBackClick = onBackClick,
        onCancelar = { id ->
            programaPorCancelar = (uiState as? ProgramasUiState.Success)?.programas?.find { it.id == id }
        },
        titulo = "Mis Programas",
        descripcion = "Estas son tus próximas jornadas agendadas.",
        mensajeVacio = "Aún no tienes voluntariados agendados.",
        snackbarHostState = snackbarHostState,
        onNavSelect = onNavSelect,
        onVerDetalle = onVerDetalle, // HU36
    )

    // Confirmación antes de cancelar: la acción libera el lugar y otra persona puede tomarlo
    programaPorCancelar?.let { programa ->
        ConfirmActionDialog(
            titulo = "¿Cancelar tu inscripción?",
            descripcion = "Si cancelas, liberarás tu lugar en ${programa.titulo}. ¿Quieres continuar?",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {
                viewModel.cancelarInscripcion(programa.id)
                programaPorCancelar = null
            },
            onDismiss = { programaPorCancelar = null },
        )
    }
}