package com.app.android.presentation.view.viewsVoluntariado

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.page.DetalleProgramaPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel

// HU36 conecta los ViewModels con la pagina.
// La inscripcion usa el ProgramasViewModel del grafo para reutilizar HU33 y su confirmacion
@Suppress("ktlint:standard:function-naming")
@Composable
fun DetalleProgramaScreen(
    programasViewModel: ProgramasViewModel,
    onBackClick: () -> Unit,
    onNavSelect: (Int) -> Unit,
    onInscripcionExitosa: () -> Unit,
    viewModel: DetalleProgramaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Errores de la inscripcion (sin cupo, ya inscrito...) en un snackbar
    LaunchedEffect(Unit) {
        programasViewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        programasViewModel.inscripcionExitosa.collect { onInscripcionExitosa() }
    }

    // Al volver a la pantalla (por ejemplo desde la confirmacion) se recarga para reflejar el cupo
    // La primera vez no, porque el ViewModel ya carga en init
    var primeraVez by rememberSaveable { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (primeraVez) primeraVez = false else viewModel.cargar()
    }

    DetalleProgramaPage(
        uiState = uiState,
        onBackClick = onBackClick,
        onReintentar = viewModel::cargar,
        onInscribirse = { detalle -> programasViewModel.inscribirse(detalle.programa.id) },
        onNavSelect = onNavSelect,
        snackbarHostState = snackbarHostState,
    )
}
