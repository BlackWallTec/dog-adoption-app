package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.ProgramasPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel

// Conecta el ViewModel (Hilt) con la página. No dibuja nada por sí misma.
@Composable
fun ProgramasScreen(
    viewModel: ProgramasViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    ProgramasPage(
        uiState = uiState,
        onReintentar = viewModel::cargarProgramas,
        onBackClick = onBackClick,
    )
}