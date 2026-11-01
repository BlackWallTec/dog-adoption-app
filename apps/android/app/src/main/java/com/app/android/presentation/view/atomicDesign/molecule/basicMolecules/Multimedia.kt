package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo
import com.app.android.presentation.view.atomicDesign.model.MediaItem

/*
 * PARÁMETROS CONFIGURABLES:
 * - mediaItem: MediaItem (Obligatorio) -> Instancia de MediaItem.Imagen o MediaItem.Video.
 * - modifier: Modifier -> Modificadores de tamaño o recorte.
 * - contentScale: ContentScale -> Ajuste de la imagen (Por defecto: Crop para llenar la vista).
 * - onVideoClick: () -> Unit -> Callback ejecutado al tocar un elemento de tipo Video.
 */
@Composable
fun Multimedia(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onVideoClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (mediaItem) {
            is MediaItem.Imagen -> {
                SubcomposeAsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(mediaItem.url)
                            .crossfade(true)
                            .build(),
                    contentDescription = "Imagen multimedia",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> EstadoCargando()
                        is AsyncImagePainter.State.Error -> EstadoError()
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }

            is MediaItem.Video -> {
                SubcomposeAsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(mediaItem.thumbnailUrl ?: mediaItem.videoUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = "Miniatura de video",
                    contentScale = contentScale,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable { onVideoClick() },
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> EstadoCargando()
                        is AsyncImagePainter.State.Error -> EstadoError()
                        else -> SubcomposeAsyncImageContent()
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Reproducir video",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
    }
}

// Comportamientos
@Composable
fun EstadoCargando() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.Gray)
    }
}

@Composable
fun EstadoError() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFFFEBEE)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = "Error al cargar imagen",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(48.dp),
            )
            TextoCuerpo(texto = "Error al cargar la imagen")
        }
    }
}

// funciones auxiliares
// Función para leer entity Dog y mapear a MediaItem
fun List<Dog>.toMediaItems(): List<MediaItem> =
    this.map { dog ->
        if (dog.tipo.equals("video", ignoreCase = true)) {
            MediaItem.Video(videoUrl = dog.url)
        } else {
            MediaItem.Imagen(url = dog.url)
        }
    }

@Preview(showBackground = true, name = "Átomo Multimedia: Imagen")
@Composable
fun MultimediaImagenPreview() {
    Multimedia(
        mediaItem = MediaItem.Imagen("https://www.magnific.com/es/fotos-vectores-gratis/perro-jpg"),
    )
}
