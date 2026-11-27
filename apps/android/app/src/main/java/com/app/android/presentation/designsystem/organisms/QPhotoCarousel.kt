// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QPhoto
import com.app.android.presentation.designsystem.model.QMediaItem
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val PHOTO_RATIO = 4f / 3f
private val DOT_SIZE = 8.dp
private val PLAY_ICON_SIZE = 48.dp

/**
 * Carrusel de fotos y videos del perro. Sin padding lateral (lo pone el template), esquinas QRadius.medium.
 * Un video muestra su miniatura con el ícono de play y toda la página llama onVideoClick. Indicador de
 * puntos solo si hay más de una página. Lista vacía: recuadro surfaceMuted con la misma proporción.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QPhoto, QIcon.
 * Reemplaza a: CarruselMultimedia, Multimedia.
 */
@Composable
fun QPhotoCarousel(
    media: List<QMediaItem>,
    modifier: Modifier = Modifier,
    onVideoClick: (QMediaItem.Video) -> Unit = {},
) {
    val colors = QTheme.colors
    if (media.isEmpty()) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .aspectRatio(PHOTO_RATIO)
                    .clip(QShapes.card)
                    .background(colors.surfaceMuted),
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { media.size })
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO),
        ) { page ->
            when (val item = media[page]) {
                is QMediaItem.Image -> {
                    QPhoto(
                        model = item.url,
                        contentDescription = "Foto ${page + 1} de ${media.size}",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is QMediaItem.Video -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(QShapes.card)
                                .clickable(role = Role.Button, onClick = { onVideoClick(item) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        QPhoto(
                            model = item.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                        QIcon(
                            icon = QIcons.Play,
                            contentDescription = "Reproducir video",
                            tint = colors.surface,
                            size = PLAY_ICON_SIZE,
                        )
                    }
                }
            }
        }

        if (media.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.xs)) {
                repeat(media.size) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier =
                            Modifier
                                .size(DOT_SIZE)
                                .clip(QShapes.pill)
                                .background(if (active) colors.onAction else colors.border),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QPhotoCarousel - estados")
@Composable
private fun QPhotoCarouselPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QPhotoCarousel(
                media =
                    listOf(
                        QMediaItem.Image("https://example.com/perro-1.jpg"),
                        QMediaItem.Image("https://example.com/perro-2.jpg"),
                        QMediaItem.Video(
                            videoUrl = "https://example.com/perro.mp4",
                            thumbnailUrl = "https://example.com/perro-thumb.jpg",
                        ),
                    ),
            )
            QPhotoCarousel(
                media = listOf(QMediaItem.Image("https://example.com/perro-1.jpg")),
            )
            QPhotoCarousel(media = emptyList())
        }
    }
}
