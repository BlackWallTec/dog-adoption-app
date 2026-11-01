package com.app.android.presentation.view.atomicDesign.organism.basicOrganisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.model.MediaItem
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.ChipTag
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.Multimedia

/*
 * ORGANISMO: CarruselMultimedia
 * Contenedor dinámico que combina HorizontalPager con el componente polimórfico Multimedia.
 *
 * PARÁMETROS:
 * - mediaList: List<MediaItem> (Obligatorio) -> Lista de imágenes/videos provenientes del ViewModel[cite: 4].
 * - modifier: Modifier -> Modificadores externos (ej. Modifier.height(300.dp)).
 * - onVideoClick: (MediaItem.Video) -> Callback al presionar "Ver video" o el botón Play.
 */
@Composable
fun CarruselMultimedia(
    mediaList: List<MediaItem>,
    modifier: Modifier = Modifier,
    onVideoClick: (MediaItem.Video) -> Unit = {},
) {
    if (mediaList.isEmpty()) return

    // Estado del Pager de Compose (Mantiene el índice de la página actual)
    val pagerState = rememberPagerState(pageCount = { mediaList.size })
    val esListaMultiple = mediaList.size > 1

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(320.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), // Redondeado inferior estilo UI[cite: 4]
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. EL CARRUSEL PRINCIPAL (Caso 5: Bucle Repetidor con HorizontalPager)[cite: 3]
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = esListaMultiple,
                modifier = Modifier.fillMaxSize(),
            ) { pageIndex ->
                val itemActual = mediaList[pageIndex]

                Multimedia(
                    mediaItem = itemActual,
                    onVideoClick = {
                        if (itemActual is MediaItem.Video) {
                            onVideoClick(itemActual)
                        }
                    },
                )
            }

            // 2. OVERLAY SUPERIOR DE CONTROLES FLOTANTES (Solo si hay múltiples elementos)[cite: 4]
            if (esListaMultiple) {
                // A. Contador numérico de páginas (esquina inferior izquierda: ej. "1/4")[cite: 4]
                ChipTag(
                    texto = "${pagerState.currentPage + 1}/${mediaList.size}",
                    colorFondo = Color.Black.copy(alpha = 0.5f),
                    colorTexto = Color.White,
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                )

                // B. Indicador visual de Puntos/Dots (Centro inferior)[cite: 4]
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(mediaList.size) { index ->
                        val esPaginaActual = pagerState.currentPage == index
                        Box(
                            modifier =
                                Modifier
                                    .size(if (esPaginaActual) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (esPaginaActual) Color(0xFF1976D2) else Color.White.copy(alpha = 0.6f),
                                    ),
                        )
                    }
                }
            }

            // C. Botón "Ver video" (si el ítem visible actual es un video)[cite: 4]
            val itemVisible = mediaList.getOrNull(pagerState.currentPage)
            if (itemVisible is MediaItem.Video) {
                ChipTag(
                    texto = "Ver video",
                    icono = Icons.Outlined.PlayCircle,
                    colorFondo = Color.White,
                    colorTexto = Color.DarkGray,
                    tintIcono = Color.DarkGray,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    conChip = true,
                )
            }
        }
    }
}

// -----------------------------------------------------------------------
// PREVISUALIZACIONES (PREVIEWS)
// -----------------------------------------------------------------------

@Preview(showBackground = true, name = "Carrusel: Lista de 4 Elementos")
@Composable
fun CarruselMultimediaPreview() {
    val sampleMedia =
        listOf(
            MediaItem.Imagen("https://images.unsplash.com/photo-1543466835-00a7907e9de1"),
            MediaItem.Imagen("https://images.unsplash.com/photo-1583511655857-d19b40a7a54e"),
            MediaItem.Video(videoUrl = "https://example.com/video.mp4"),
            MediaItem.Imagen("https://images.unsplash.com/photo-1537151608828-ea2b11777ee8"),
        )

    CarruselMultimedia(mediaList = sampleMedia)
}

@Preview(showBackground = true, name = "Carrusel: Solo 1 Imagen")
@Composable
fun CarruselMultimediaUnicoItemPreview() {
    val sampleMedia =
        listOf(
            MediaItem.Imagen("https://images.unsplash.com/photo-1543466835-00a7907e9de1"),
        )

    CarruselMultimedia(mediaList = sampleMedia)
}
