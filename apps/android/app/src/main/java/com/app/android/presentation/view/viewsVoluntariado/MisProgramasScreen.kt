package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.designsystem.pages.QMisVoluntariadosPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.MisProgramasViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import kotlinx.coroutines.delay

private const val MESSAGE_DURATION_MS = 4_000L

// Conecta el ViewModel (Hilt) con la página nueva. No dibuja nada por sí misma.
@Suppress("ktlint:standard:function-naming")
@Composable
fun MisProgramasScreen(
    viewModel: MisProgramasViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {},
    onVerDetalle: ((String) -> Unit)? = null, // HU36: id del programa
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mensaje by remember { mutableStateOf<String?>(null) }

    // Guarda el último mensaje de cancelación y lo quita solo después de unos segundos.
    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { mensaje = it }
    }
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(MESSAGE_DURATION_MS)
            mensaje = null
        }
    }

    QMisVoluntariadosPage(
        isLoading = uiState is ProgramasUiState.Loading,
        errorMessage = (uiState as? ProgramasUiState.Error)?.mensaje,
        programs = (uiState as? ProgramasUiState.Success)?.programas.orEmpty(),
        onBack = onBackClick,
        onRetry = viewModel::cargarProgramas,
        onProgramClick = { id -> onVerDetalle?.invoke(id) }, // HU36
        // La página ya pide confirmación antes de llamar a este callback.
        onCancelEnrollment = viewModel::cancelarInscripcion,
        onTabSelected = { tab -> onNavSelect(tab.ordinal) },
        message = mensaje,
    )
}