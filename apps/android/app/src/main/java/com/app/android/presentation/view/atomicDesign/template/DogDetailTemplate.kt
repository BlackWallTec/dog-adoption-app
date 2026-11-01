package com.app.android.presentation.view.atomicDesign.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.StickyAnchor
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.StickyButton
import com.app.android.presentation.view.atomicDesign.model.MediaItem
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.CarruselMultimedia
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.DogDetailState
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.SeccionInfoTarjeta
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.TarjetaInfo
import com.app.android.presentation.view.ui.theme.AndroidTheme

/*
 * PLANTILLA (Template): DogDetailTemplate
 * Contenedor estructural que define el layout de la pantalla de detalle.
 */
@Composable
fun DogDetailTemplate(
    mediaList: List<MediaItem>,
    dogState: DogDetailState,
    onAdoptClick: () -> Unit,
    modifier: Modifier = Modifier,
    ordenSecciones: List<SeccionInfoTarjeta> = SeccionInfoTarjeta.entries,
    onVideoClick: (MediaItem.Video) -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        // 1. CONTENIDO SCROLLABLE (Carrusel + Tarjeta de Info)
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    // Padding inferior extra para que el StickyButton no tape el texto final
                    .padding(bottom = 100.dp),
        ) {
            // Organismo 1: Carrusel de fotos/videos
            CarruselMultimedia(
                mediaList = mediaList,
                onVideoClick = onVideoClick,
            )

            // Organismo 2: Tarjeta con toda la información
            TarjetaInfo(
                dogState = dogState,
                ordenSecciones = ordenSecciones,
                modifier = Modifier.padding(16.dp),
            )
        }

        // 2. ELEMENTO FLOTANTE ANCLADO (Molécula StickyButton)
        StickyButton(
            text = "¡Quiero Adoptar!",
            onClick = onAdoptClick,
            anchor = StickyAnchor.BOTTOM_FILL,
            conDegradadoFondo = false,
        )
    }
}

// Preview directa del esqueleto visual
@Preview(showBackground = true, name = "Plantilla: Detalle de Perro")
@Composable
fun DogDetailTemplatePreview() {
    AndroidTheme {
        DogDetailTemplate(
            mediaList =
                listOf(
                    MediaItem.Imagen("https://images.unsplash.com/photo-1543466835-00a7907e9de1"),
                ),
            dogState =
                DogDetailState(
                    nombre = "Bruno",
                    estado = "Disponible",
                    ubicacion = "Querétaro, Qro.",
                    metricas = mapOf("Edad" to "2 años", "Sexo" to "Macho", "Tamaño" to "Mediano", "Raza" to "Mestizo"),
                    salud = listOf("Vacunado", "Esterilizado"),
                    caracteristicas = listOf("Convive con gatos", "Cariñoso"),
                    sobreMi = "¡Hola, soy Bruno! Me encantan los paseos y jugar.",
                ),
            onAdoptClick = {},
        )
    }
}
