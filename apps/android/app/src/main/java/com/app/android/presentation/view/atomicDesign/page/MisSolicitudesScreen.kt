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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.IconWithBadge
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.EstadoTarjeta
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.TarjetaSolicitud
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// Lo que la pantalla necesita de cada solicitud (ya listo para mostrar).
// Despues el ViewModel convertira lo del domain a esto.
data class SolicitudItem(
    val idSolicitud: String,
    val nombrePerro: String,
    val fotoUrl: String?, // null = usa la imagen de prueba (que tenemos que cambiar que es lucca jaajjjajaj)
    val folio: String,
    val fecha: String,
    val estado: EstadoTarjeta,
)

// PAGE: Mis solicitudes (stateless: todo llega por parametros)
@Composable
fun MisSolicitudesScreen(
    solicitudes: List<SolicitudItem>,
    onSolicitudClick: (idSolicitud: String) -> Unit,
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
        if (solicitudes.isEmpty()) {
            SinSolicitudes(
                onVerCatalogoClick = onVerCatalogoClick,
                modifier = Modifier.padding(paddingInterno),
            )
        } else {
            // LazyColumn = lista con scroll que solo dibuja lo que se ve (mejor para estou)
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingInterno),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Encabezado() }

                items(items = solicitudes, key = { it.idSolicitud }) { solicitud ->
                    // Foto; si no hay URL o falla, la de prueba
                    val respaldo = painterResource(R.drawable.perro_prueba)
                    val foto =
                        rememberAsyncImagePainter(
                            model = solicitud.fotoUrl,
                            placeholder = respaldo,
                            error = respaldo,
                            fallback = respaldo,
                        )

                    TarjetaSolicitud(
                        foto = foto,
                        nombre = solicitud.nombrePerro,
                        folio = solicitud.folio,
                        fecha = solicitud.fecha,
                        estado = solicitud.estado,
                        onEditar = { onEditarClick(solicitud.idSolicitud) },
                        onClick = { onSolicitudClick(solicitud.idSolicitud) },
                    )
                }
            }
        }
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
        SolicitudItem("1", "Bruno", null, "QA-0248", "12 oct 2026", EstadoTarjeta.EN_REVISION),
        SolicitudItem("2", "Luna", null, "QA-0231", "10 oct 2026", EstadoTarjeta.DOCUMENTO_RECHAZADO),
        SolicitudItem("3", "Milo", null, "QA-0210", "28 sep 2026", EstadoTarjeta.APROBADA),
        SolicitudItem("4", "Nala", null, "QA-0185", "20 sep 2026", EstadoTarjeta.RECHAZADA),
    )

@Preview(showBackground = true, name = "Mis solicitudes", heightDp = 900)
@Composable
fun MisSolicitudesScreenPreview() {
    AndroidTheme {
        MisSolicitudesScreen(
            solicitudes = solicitudesPrueba,
            onSolicitudClick = {},
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
            solicitudes = emptyList(),
            onSolicitudClick = {},
            onEditarClick = {},
            onVerCatalogoClick = {},
        )
    }
}