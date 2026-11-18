package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.MarcaPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atomic.EtiquetaTipo
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.EtiquetaEstado
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.TarjetaFechaLugar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.viewsVoluntariado.cupoTexto
import com.app.android.presentation.view.viewsVoluntariado.fechaDetalleTexto
import com.app.android.presentation.view.viewsVoluntariado.horaFinTexto
import com.app.android.presentation.view.viewsVoluntariado.horaInicioTexto
import com.app.android.presentation.view.viewsVoluntariado.progresoCupo
import com.app.android.presentation.view.viewsVoluntariado.texto
import com.app.android.presentation.view.viewsVoluntariado.ubicacionTexto
import com.app.android.presentation.viewmodel.viewModelVoluntariado.DetalleProgramaUiState
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val AVISO_COMPROMISO =
    "Al inscribirte te comprometes a asistir. Si no puedes venir, avisa al equipo con anticipación."

// HU36 pantalla de detalle. Solo dibuja el estado y la logica esta en el ViewModel
@Composable
fun DetalleProgramaPage(
    uiState: DetalleProgramaUiState,
    onBackClick: () -> Unit,
    onReintentar: () -> Unit,
    onInscribirse: (DetallePrograma) -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 1,
    onNavSelect: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colores = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colores.surface,
        topBar = { AppTopBar(titulo = "Detalle del programa", onBackClick = onBackClick) },
        bottomBar = { NavBar(selected = navSeleccionado, onSelect = onNavSelect) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingInterno ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(horizontal = 16.dp),
        ) {
            when (uiState) {
                is DetalleProgramaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DetalleProgramaUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ErrorMessage(text = uiState.mensaje)
                        Spacer(modifier = Modifier.height(12.dp))
                        AppButton(text = "Reintentar", onClick = onReintentar)
                    }
                }

                is DetalleProgramaUiState.NoEncontrado -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        InfoMessageCard(mensaje = "Este programa ya no está disponible.", icono = Icons.Outlined.Info)
                        Spacer(modifier = Modifier.height(12.dp))
                        AppButton(text = "Volver a programas", onClick = onBackClick)
                    }
                }

                is DetalleProgramaUiState.Success -> {
                    ContenidoDetalle(
                        detalle = uiState.detalle,
                        accion = uiState.accion,
                        onVolver = onBackClick,
                        onInscribirse = { onInscribirse(uiState.detalle) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    detalle: DetallePrograma,
    accion: AccionDetalle,
    onVolver: () -> Unit,
    onInscribirse: () -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val programa = detalle.programa

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onVolver),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colores.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(text = "Volver a programas", color = colores.inverseSurface, fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaTipo(texto = programa.tipo)
            if (accion is AccionDetalle.SinAccion) {
                EtiquetaEstado(
                    texto = accion.marca.texto(),
                    icono = Icons.Outlined.Info,
                    colorFondo = colores.surfaceContainerHighest,
                    colorContenido = colores.inverseSurface,
                )
            }
        }

        detalle.tipoDescripcion?.let {
            Text(text = it, color = colores.inverseSurface.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        Text(text = programa.nombre, color = colores.inverseSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Text(text = "Sobre el programa", color = colores.inverseSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        // Text de Compose respeta los saltos de línea y no interpreta HTML
        Text(
            text = programa.descripcion?.takeIf { it.isNotBlank() } ?: "Este programa no tiene descripción.",
            color = colores.inverseSurface,
            fontSize = 16.sp,
        )

        TarjetaFechaLugar(
            fecha = detalle.fechaDetalleTexto(),
            horaInicio = detalle.horaInicioTexto(),
            horaFin = detalle.horaFinTexto(),
            ubicacion = programa.ubicacionTexto(),
            textoCupo = detalle.cupoTexto(),
            progresoCupo = detalle.progresoCupo(),
        )

        if (accion !is AccionDetalle.SinAccion) {
            InfoMessageCard(mensaje = AVISO_COMPROMISO, icono = Icons.Outlined.Info)
        }

        when (accion) {
            AccionDetalle.Inscribirme -> {
                AppButton(text = "Inscribirme", onClick = onInscribirse, modifier = Modifier.fillMaxWidth())
            }

            AccionDetalle.YaInscrito -> {
                AppButton(text = "Ya estás inscrito", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
            }

            AccionDetalle.CupoLleno -> {
                AppButton(text = "Cupo lleno", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
            }

            is AccionDetalle.SinAccion -> {
                Unit
            }
        }
    }
}

// PREVISUALIZACIONES
private fun detalleEjemplo(): DetallePrograma {
    val inicio = LocalDateTime.of(2026, 12, 10, 9, 0).toInstant(ZoneOffset.ofHours(-6))
    val fin = LocalDateTime.of(2026, 12, 10, 11, 0).toInstant(ZoneOffset.ofHours(-6))
    return DetallePrograma(
        programa =
            Programa(
                id = "1",
                nombre = "Paseos con cariño",
                tipo = "Paseo",
                descripcion = "Acompaña a los perros del refugio en un paseo tranquilo.\n\nRopa cómoda, zapatos cerrados y agua.",
                fechaInicio = inicio,
                fechaFin = fin,
                ubicacion = "Refugio Qariño · Querétaro",
                cupoMaximo = 15,
                cupoDisponible = 7,
                estado = EstadoPrograma.DISPONIBLE,
                usuarioInscrito = false,
            ),
        tipoDescripcion = "Paseos y socialización con los perros",
        inscritos = 8,
        estadoGuardado = "Disponible",
    )
}

@Preview(showBackground = true, name = "Detalle - Inscribirme")
@Composable
private fun DetalleProgramaPageInscribirmePreview() {
    AndroidTheme {
        DetalleProgramaPage(
            uiState = DetalleProgramaUiState.Success(detalleEjemplo(), AccionDetalle.Inscribirme),
            onBackClick = {},
            onReintentar = {},
            onInscribirse = {},
        )
    }
}

@Preview(showBackground = true, name = "Detalle - Cancelado")
@Composable
private fun DetalleProgramaPageCanceladoPreview() {
    AndroidTheme {
        DetalleProgramaPage(
            uiState =
                DetalleProgramaUiState.Success(
                    detalleEjemplo(),
                    AccionDetalle.SinAccion(MarcaPrograma.CANCELADO),
                ),
            onBackClick = {},
            onReintentar = {},
            onInscribirse = {},
        )
    }
}

@Preview(showBackground = true, name = "Detalle - No encontrado")
@Composable
private fun DetalleProgramaPageNoEncontradoPreview() {
    AndroidTheme {
        DetalleProgramaPage(
            uiState = DetalleProgramaUiState.NoEncontrado,
            onBackClick = {},
            onReintentar = {},
            onInscribirse = {},
        )
    }
}
