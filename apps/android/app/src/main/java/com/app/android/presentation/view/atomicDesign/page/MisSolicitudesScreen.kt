package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.app.android.R
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.IconWithBadge
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.EstadoTarjeta
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.TarjetaSolicitudExpandible
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.viewmodel.viewModelSolicitudes.SolicitudItem

// PAGE: Mis solicitudes (stateless: todo llega por parametros)
// SolicitudItem ahora vive en viewmodel (MisSolicitudesUiState.kt)
@Composable
fun MisSolicitudesScreen(
    cargando: Boolean,
    errorCarga: String?,
    solicitudes: List<SolicitudItem>,
    onReintentar: () -> Unit,
    onEditarClick: (idSolicitud: String) -> Unit,
    onVerCatalogoClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificaciones: Int = 0,
    onNotificacionesClick: () -> Unit = {},
    navSeleccionado: Int = 3, // 3 = "Solicitudes"
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        // Esta pantalla no tiene flecha de regreso: solo la campanita a la derecha
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconWithBadge(
                    icon = Icons.Outlined.Notifications,
                    number = notificaciones,
                    onClick = onNotificacionesClick,
                )
            }
        },
        bottomBar = { NavBar(selected = navSeleccionado, onSelect = onNavSelect) },
    ) { paddingInterno ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
        ) {
            when {
                // 1 Loading -> circulito al centro
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // 2 Error -> mensaje y botón para reintentar
                errorCarga != null -> {
                    ErrorCarga(
                        mensaje = errorCarga,
                        onReintentar = onReintentar,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // 3 Sin solicitudes -> invitación al catálogo
                solicitudes.isEmpty() -> {
                    SinSolicitudes(onVerCatalogoClick = onVerCatalogoClick)
                }

                // 4 Lista
                else -> {
                    // LazyColumn = lista con scroll que solo dibuja lo que se ve (mejor para estou)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { Encabezado() }

                        items(items = solicitudes, key = { it.idSolicitud }) { solicitud ->
                            // Si la tarjeta está abierta o no es solo de la pantalla (no pasa por el ViewModel)
                            // rememberSaveable para que no se cierre al girar el teléfono
                            var expandida by rememberSaveable { mutableStateOf(false) }

                            // Foto; si no hay URL o falla, la de prueba
                            val respaldo = painterResource(R.drawable.perro_prueba)
                            val foto =
                                rememberAsyncImagePainter(
                                    model = solicitud.fotoUrl,
                                    placeholder = respaldo,
                                    error = respaldo,
                                    fallback = respaldo,
                                )

                            TarjetaSolicitudExpandible(
                                foto = foto,
                                nombre = solicitud.nombrePerro,
                                folio = solicitud.folio,
                                fecha = solicitud.fecha,
                                estado = solicitud.toEstadoTarjeta(),
                                comentarioAdmin = solicitud.comentarioAdmin,
                                puedeEditar = solicitud.puedeEditar,
                                expandida = expandida,
                                onToggleDetalles = { expandida = !expandida },
                                onEditar = { onEditarClick(solicitud.idSolicitud) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// Traduce lo que manda el ViewModel (sin nada visual) al estilo de la tarjeta.
// El documento rechazado solo cuenta mientras la solicitud sigue en revisión.
private fun SolicitudItem.toEstadoTarjeta(): EstadoTarjeta =
    if (documentoRechazado && estado == EstadoSolicitud.PENDIENTE_REVISION) {
        EstadoTarjeta.DOCUMENTO_RECHAZADO
    } else {
        when (estado) {
            EstadoSolicitud.PENDIENTE_REVISION -> EstadoTarjeta.EN_REVISION
            EstadoSolicitud.EN_PERIODO_PRUEBA -> EstadoTarjeta.EN_PERIODO_PRUEBA
            EstadoSolicitud.APROBADA -> EstadoTarjeta.APROBADA
            EstadoSolicitud.RECHAZADA -> EstadoTarjeta.RECHAZADA
            EstadoSolicitud.DESCONOCIDO -> EstadoTarjeta.EN_PROCESO
        }
    }

// Título + subtítulo (privado: solo lo usa esta pantalla)
@Composable
private fun Encabezado() {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
        Text(
            text = "Mis solicitudes",
            color = InverseSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Consulta el estado de tus solicitudes de adopción.",
            color = InverseSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

// Cuando no se pudieron cargar las solicitudes (sin conexión o error del servidor)
@Composable
private fun ErrorCarga(
    mensaje: String,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = mensaje,
            color = InverseSurface,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        AppButton(text = "Reintentar", onClick = onReintentar)
    }
}

// Cuando todavia no hay o no ha hecho ninguna solicitud
@Composable
private fun SinSolicitudes(
    onVerCatalogoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Encabezado()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Aún no tienes solicitudes.\n¡Encuentra a tu nuevo amigo en el catálogo!",
                    color = InverseSurface,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
                AppButton(text = "Ver catálogo", onClick = onVerCatalogoClick)
            }
        }
    }
}

// ===== PREVIEWS =====

private val solicitudesPrueba =
    listOf(
        SolicitudItem(
            "1",
            "Bruno",
            null,
            "QA-0248",
            "12 oct 2026",
            EstadoSolicitud.PENDIENTE_REVISION,
            false,
            false,
            null,
        ),
        SolicitudItem(
            "2",
            "Luna",
            null,
            "QA-0231",
            "10 oct 2026",
            EstadoSolicitud.PENDIENTE_REVISION,
            true,
            true,
            "La foto de tu INE sale borrosa, súbela de nuevo por favor.",
        ),
        SolicitudItem(
            "3",
            "Milo",
            null,
            "QA-0210",
            "28 sep 2026",
            EstadoSolicitud.APROBADA,
            false,
            false,
            null,
        ),
        SolicitudItem(
            "4",
            "Nala",
            null,
            "QA-0185",
            "20 sep 2026",
            EstadoSolicitud.RECHAZADA,
            false,
            false,
            "Por ahora no cumples con el espacio requerido para esta mascota.",
        ),
    )

@Preview(showBackground = true, name = "Mis solicitudes", heightDp = 900)
@Composable
fun MisSolicitudesScreenPreview() {
    AndroidTheme {
        MisSolicitudesScreen(
            cargando = false,
            errorCarga = null,
            solicitudes = solicitudesPrueba,
            onReintentar = {},
            onEditarClick = {},
            onVerCatalogoClick = {},
            notificaciones = 2,
        )
    }
}

@Preview(showBackground = true, name = "Mis solicitudes - vacío", heightDp = 900)
@Composable
fun MisSolicitudesVaciaPreview() {
    AndroidTheme {
        MisSolicitudesScreen(
            cargando = false,
            errorCarga = null,
            solicitudes = emptyList(),
            onReintentar = {},
            onEditarClick = {},
            onVerCatalogoClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Mis solicitudes - cargando", heightDp = 900)
@Composable
fun MisSolicitudesCargandoPreview() {
    AndroidTheme {
        MisSolicitudesScreen(
            cargando = true,
            errorCarga = null,
            solicitudes = emptyList(),
            onReintentar = {},
            onEditarClick = {},
            onVerCatalogoClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Mis solicitudes - error", heightDp = 900)
@Composable
fun MisSolicitudesErrorPreview() {
    AndroidTheme {
        MisSolicitudesScreen(
            cargando = false,
            errorCarga = "No se pudieron cargar tus solicitudes. Revisa tu conexión.",
            solicitudes = emptyList(),
            onReintentar = {},
            onEditarClick = {},
            onVerCatalogoClick = {},
        )
    }
}
