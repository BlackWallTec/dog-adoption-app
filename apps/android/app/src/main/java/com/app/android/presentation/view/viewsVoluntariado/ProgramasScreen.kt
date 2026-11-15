package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
    onMisVoluntariados: () -> Unit = {},
    onVerDetalle: ((String) -> Unit)? = null, // HU36: id del programa
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtrosState by viewModel.filtroState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var programaPorCancelar by remember {
        mutableStateOf<ProgramaUi?>(null)
    }

    // Muestra los mensajes de inscripción y cancelación.
    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    // Navega a la confirmación cuando termina la inscripción.
    LaunchedEffect(viewModel, onInscripcionExitosa) {
        viewModel.inscripcionExitosa.collect {
            onInscripcionExitosa()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.actualizarProgramas() }

    ProgramasPage(
        uiState = uiState,
        onReintentar = viewModel::cargarProgramas,
        onBackClick = onBackClick,
        onInscribirse = viewModel::inscribirse,
        onCancelar = { id ->
            programaPorCancelar =
                (uiState as? ProgramasUiState.Success)
                    ?.programas
                    ?.find { it.id == id }
        },
        snackbarHostState = snackbarHostState,
        onNavSelect = onNavSelect,
        onMisVoluntariados = onMisVoluntariados,
        onVerDetalle = onVerDetalle, // HU36
        filtrosState = filtrosState,
        onFiltrosChange = viewModel::actualizarFiltros,
        onAplicarFiltros = viewModel::aplicarFiltros,
        onLimpiarFiltros = viewModel::limpiarFiltros,
    )

    // Solicita confirmación antes de cancelar la inscripción.
    programaPorCancelar?.let { programa ->
        ConfirmActionDialog(
            titulo = "¿Cancelar tu inscripción?",
            descripcion =
                "Si cancelas, liberarás tu lugar en ${programa.titulo}. " +
                        "¿Quieres continuar?",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {
                viewModel.cancelarInscripcion(programa.id)
                programaPorCancelar = null
            },
            onDismiss = {
                programaPorCancelar = null
            },
        )
    }
}
