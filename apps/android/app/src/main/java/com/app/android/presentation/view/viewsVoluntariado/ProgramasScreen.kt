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
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel

// Conecta el ViewModel (Hilt) con la página. No dibuja nada por sí misma.
@Suppress("ktlint:standard:function-naming")
@Composable
fun ProgramasScreen(
    viewModel: ProgramasViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {},
    onInscripcionExitosa: () -> Unit = {},
) {
    // Guía: collectAsStateWithLifecycle pausa la recolección cuando la pantalla no está visible
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var programaPorCancelar by remember { mutableStateOf<ProgramaUi?>(null) }

    // Muestra en un snackbar los mensajes de la inscripción y la cancelación
    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.inscripcionExitosa.collect { onInscripcionExitosa() }
    }

    ProgramasPage(
        uiState = uiState,
        onReintentar = viewModel::cargarProgramas,
        onBackClick = onBackClick,
        onInscribirse = viewModel::inscribirse,
        onCancelar = { id ->
            programaPorCancelar = (uiState as? ProgramasUiState.Success)?.programas?.find { it.id == id }
        },
        snackbarHostState = snackbarHostState,
        onNavSelect = onNavSelect,
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
