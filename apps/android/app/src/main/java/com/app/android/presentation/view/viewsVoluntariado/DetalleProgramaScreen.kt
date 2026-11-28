package com.app.android.presentation.view.viewsVoluntariado


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
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.presentation.designsystem.pages.QDetalleProgramaPage
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel
import kotlinx.coroutines.delay


// Cuánto se ve el aviso de la inscripción antes de quitarse solo
private const val DURACION_MENSAJE_MS = 4_000L

// Si el ViewModel ignora el intento (ya hay otro proceso en curso o el programa no está en su lista) no emite nada; con este límite el botón no se queda cargando para siempre.
private const val LIMITE_INSCRIPCION_MS = 15_000L
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
    // Estado visual de la Route: el botón en carga y el último aviso de la inscripción
    var inscribiendo by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    // Errores de la inscripcion (sin cupo, ya inscrito...) se muestran arriba con QInfoMessage
    LaunchedEffect(Unit) {
        programasViewModel.mensajes.collect {
            inscribiendo = false
            mensaje = it
        }
    }
    LaunchedEffect(Unit) {
        programasViewModel.inscripcionExitosa.collect {
            inscribiendo = false
            onInscripcionExitosa()
        }
    }
    // El aviso se quita solo después de unos segundos
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(DURACION_MENSAJE_MS)
            mensaje = null
        }
    }
    LaunchedEffect(inscribiendo) {
        if (inscribiendo) {
            delay(LIMITE_INSCRIPCION_MS)
            inscribiendo = false
        }
    }

    // Al volver a la pantalla (por ejemplo desde la confirmacion) se recarga para reflejar el cupo
    // La primera vez no, porque el ViewModel ya carga en init
    var primeraVez by rememberSaveable { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (primeraVez) primeraVez = false else viewModel.cargar()
    }


    // Copia local: uiState es delegado y no permite smart cast
    val estado = uiState
    val exito = estado as? DetalleProgramaUiState.Success
    val detalle = exito?.detalle
    val programa = detalle?.programa

    // Mientras no hay Success los textos van vacíos: la página muestra carga, error o no encontrado
    QDetalleProgramaPage(
        isLoading = estado is DetalleProgramaUiState.Loading,
        errorMessage = (estado as? DetalleProgramaUiState.Error)?.mensaje,
        isNotFound = estado is DetalleProgramaUiState.NoEncontrado,
        type = programa?.tipo.orEmpty(),
        title = programa?.nombre.orEmpty(),
        description = programa?.descripcion,
        date = detalle?.fechaDetalleTexto().orEmpty(),
        time = detalle?.let { "${it.horaInicioTexto()} – ${it.horaFinTexto()} h" }.orEmpty(),
        location = programa?.ubicacionTexto().orEmpty(),
        capacityText = detalle?.cupoTexto().orEmpty(),
        capacityProgress = detalle?.progresoCupo() ?: 0f,
        action = exito?.accion ?: AccionDetalle.Inscribirme,
        onBack = onBackClick,
        onRetry = viewModel::cargar,
        onEnroll = {
            if (programa != null) {
                mensaje = null // el siguiente intento borra el aviso anterior
                inscribiendo = true
                programasViewModel.inscribirse(programa.id)
            }
        },
        onTabSelected = { onNavSelect(it.ordinal) },
        typeDescription = detalle?.tipoDescripcion,
        isEnrolling = inscribiendo,
        message = mensaje,
    )
}