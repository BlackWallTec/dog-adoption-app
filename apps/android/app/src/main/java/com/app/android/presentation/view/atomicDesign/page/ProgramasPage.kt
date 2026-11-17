package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.FiltrosProgramasPanel
import com.app.android.presentation.view.atomicDesign.organism.ListaProgramas
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelVoluntariado.FiltrosProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState

@Composable
fun ProgramasPage(
    uiState: ProgramasUiState,
    onReintentar: () -> Unit,
    onBackClick: () -> Unit,
    onInscribirse: (String) -> Unit = {},
    onCancelar: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 1,
    onNavSelect: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    titulo: String = "Programas",
    descripcion: String = "Elige un programa de voluntariado para ayudar a los perritos de Qariño.",
    mensajeVacio: String = "No hay programas disponibles por ahora.",
    onMisVoluntariados: (() -> Unit)? = null,
    onVerDetalle: ((String) -> Unit)? = null,
    filtrosState: FiltrosProgramasUiState = FiltrosProgramasUiState(),
    onFiltrosChange: (FiltrosProgramas) -> Unit = {},
    onAplicarFiltros: () -> Unit = {},
    onLimpiarFiltros: () -> Unit = {},
) {
    val colores = MaterialTheme.colorScheme

    val programas: List<ProgramaUi> =
        if (uiState is ProgramasUiState.Success) {
            uiState.programas
        } else {
            emptyList()
        }

    Scaffold(
        modifier = modifier,
        containerColor = colores.surface,
        topBar = {
            AppTopBar(
                titulo = "Voluntariado",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { paddingInterno ->
        ListaProgramas(
            programas = programas,
            onInscribirse = onInscribirse,
            onCancelar = onCancelar,
            onVerDetalle = onVerDetalle,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(horizontal = 16.dp),
            encabezado = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = titulo,
                        color = colores.inverseSurface,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    Text(
                        text = descripcion,
                        color = colores.inverseSurface,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                    )

                    onMisVoluntariados?.let { alTocar ->
                        AppButton(
                            text = "Mis voluntariados",
                            onClick = alTocar,
                            variant = ButtonVariant.Outlined,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                        )
                    }

                    if (
                        uiState is ProgramasUiState.Success ||
                        uiState is ProgramasUiState.Empty
                    ) {
                        FiltrosProgramasPanel(
                            estado = filtrosState,
                            onFiltrosChange = onFiltrosChange,
                            onAplicarFiltros = onAplicarFiltros,
                            onLimpiarFiltros = onLimpiarFiltros,
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    when (uiState) {
                        is ProgramasUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                        }

                        is ProgramasUiState.Empty -> {
                            InfoMessageCard(
                                mensaje = mensajeVacio,
                                icono = Icons.Outlined.Info,
                            )
                        }

                        is ProgramasUiState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ErrorMessage(text = uiState.mensaje)

                                Spacer(modifier = Modifier.height(12.dp))

                                AppButton(
                                    text = "Reintentar",
                                    onClick = onReintentar,
                                )
                            }
                        }

                        is ProgramasUiState.Success -> {
                            if (filtrosState.sinCoincidencias) {
                                InfoMessageCard(
                                    mensaje = "No hay programas en esas fechas u horario.",
                                    icono = Icons.Outlined.Info,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                AppButton(
                                    text = "Limpiar filtros",
                                    onClick = onLimpiarFiltros,
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true, name = "Programas - lista")
@Composable
private fun ProgramasPageSuccessPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState =
                ProgramasUiState.Success(
                    listOf(
                        programaUiDePrueba(
                            id = "1",
                            titulo = "Bazar de Donativos",
                            textoCupo = "8/15 lugares ocupados · 7 disponibles",
                            progresoCupo = 8f / 15f,
                            textoBoton = "Inscribirme",
                            botonHabilitado = true,
                        ),
                        programaUiDePrueba(
                            id = "2",
                            titulo = "Jornada de limpieza",
                            textoCupo = "10/10 lugares ocupados · 0 disponibles",
                            progresoCupo = 1f,
                            textoBoton = "Sin lugares",
                            botonHabilitado = false,
                        ),
                    ),
                ),
            onReintentar = {},
            onBackClick = {},
            onVerDetalle = {},
            onMisVoluntariados = {},
        )
    }
}

@Preview(showBackground = true, name = "Mis Programas - vacío")
@Composable
private fun MisProgramasEmptyPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState = ProgramasUiState.Empty,
            onReintentar = {},
            onBackClick = {},
            titulo = "Mis Programas",
            descripcion = "Estas son tus próximas jornadas agendadas.",
            mensajeVacio = "Aún no tienes voluntariados agendados.",
        )
    }
}

@Preview(showBackground = true, name = "Programas - vacío")
@Composable
private fun ProgramasPageEmptyPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState = ProgramasUiState.Empty,
            onReintentar = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Programas - error")
@Composable
private fun ProgramasPageErrorPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState =
                ProgramasUiState.Error(
                    "No se pudieron cargar los programas",
                ),
            onReintentar = {},
            onBackClick = {},
        )
    }
}

private fun programaUiDePrueba(
    id: String,
    titulo: String,
    textoCupo: String,
    progresoCupo: Float,
    textoBoton: String,
    botonHabilitado: Boolean,
) = ProgramaUi(
    id = id,
    tipo = "Cuidado de Perros",
    titulo = titulo,
    fecha = "Vie. 11 dic. 2026",
    horario = "12:17 – 15:17 h",
    ubicacion = "Refugio Qariño · Querétaro",
    textoCupo = textoCupo,
    progresoCupo = progresoCupo,
    textoBoton = textoBoton,
    botonHabilitado = botonHabilitado,
)