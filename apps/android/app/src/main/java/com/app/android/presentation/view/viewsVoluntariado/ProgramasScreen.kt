package com.app.android.presentation.view.viewsVoluntariado

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
import com.app.android.presentation.designsystem.pages.QProgramasPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel
import kotlinx.coroutines.delay

private const val MESSAGE_DURATION_MS = 4_000L

// Conecta el ViewModel (Hilt) con la página nueva. No dibuja nada por sí misma.
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
    var mensaje by remember { mutableStateOf<String?>(null) }

    // Guarda el último mensaje de inscripción o cancelación y lo quita solo después de unos segundos.
    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { mensaje = it }
    }
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(MESSAGE_DURATION_MS)
            mensaje = null
        }
    }

    // Navega a la confirmación cuando termina la inscripción.
    LaunchedEffect(viewModel, onInscripcionExitosa) {
        viewModel.inscripcionExitosa.collect {
            onInscripcionExitosa()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.actualizarProgramas() }

    QProgramasPage(
        isLoading = uiState is ProgramasUiState.Loading,
        errorMessage = (uiState as? ProgramasUiState.Error)?.mensaje,
        programs = (uiState as? ProgramasUiState.Success)?.programas.orEmpty(),
        filters = filtrosState,
        onFiltersChange = viewModel::actualizarFiltros,
        onApplyFilters = viewModel::aplicarFiltros,
        onClearFilters = viewModel::limpiarFiltros,
        onRetry = viewModel::cargarProgramas,
        onProgramClick = { id -> onVerDetalle?.invoke(id) }, // HU36
        onEnroll = viewModel::inscribirse,
        // La página ya pide confirmación antes de llamar a este callback.
        onCancel = viewModel::cancelarInscripcion,
        onMyVolunteeringClick = onMisVoluntariados,
        onTabSelected = { tab -> onNavSelect(tab.ordinal) },
        message = mensaje,
    )
}