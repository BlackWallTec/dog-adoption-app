package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.ProgramasPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel

// Conecta el ViewModel (Hilt) con la página. No dibuja nada por sí misma.
@Composable
fun ProgramasScreen(
    viewModel: ProgramasViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {},
    onInscripcionExitosa: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Muestra en un snackbar los mensajes de la inscripción
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
        snackbarHostState = snackbarHostState,
        onNavSelect = onNavSelect,
    )
}