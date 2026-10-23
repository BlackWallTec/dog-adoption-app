package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.BotonNavegacion
import com.app.android.presentation.view.atomicDesign.atomic.SurfaceContainer

// Los datos de cada botón: solo texto y el icono
data class OpcionNav(
    val etiqueta: String,
    val icono: ImageVector,
)

// La lista de los botones en orden
val opcionesNav =
    listOf(
        OpcionNav("Catálogo", Icons.Filled.Pets),
        OpcionNav("Ayudar", Icons.Outlined.VolunteerActivism),
        OpcionNav("Donar", Icons.Outlined.FavoriteBorder),
        OpcionNav("Solicitudes", Icons.AutoMirrored.Outlined.Assignment),
        OpcionNav("Ajustes", Icons.Outlined.Settings),
    )

// 1. EL ORGANISMO: junta los 5 atomos en una fila
@Composable
fun BarraNavegacion(
    seleccionado: Int, // posición del botón activo (0 = Catalogo, 4 = Ajustes)
    alSeleccionar: (Int) -> Unit, // avisa cual se toco
    modifier: Modifier = Modifier,
) {
    // Row = acomoda las cosas una al lado de otra (como column pero horizontal duh)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SurfaceContainer)
                .padding(vertical = 4.dp),
    ) {
        // Por cada opción de la lista, dibuja un BotonNavegacion
        opcionesNav.forEachIndexed { index, opcion ->
            BotonNavegacion(
                etiqueta = opcion.etiqueta,
                icono = opcion.icono,
                seleccionado = index == seleccionado,
                alHacerClick = { alSeleccionar(index) },
                modifier = Modifier.weight(1f), // los 5 ocupan el mismo ancho
            )
        }
    }
}

// 2. PREVISUALIZACIÓN: con estado, para poder darle clic en modo interactivo
@Preview(showBackground = true, name = "Barra de navegación")
@Composable
fun BarraNavegacionPreview() {
    var seleccionado by remember { mutableIntStateOf(0) }
    BarraNavegacion(
        seleccionado = seleccionado,
        alSeleccionar = { seleccionado = it },
    )
}