// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.designsystem.model.QMediaItem

/**
 * Carrusel de fotos/videos del perro con padding lateral (lo pone el template) y esquinas
 * QRadius.medium; indicador de página abajo. Una sola foto = sin indicador.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QPhoto, QIcon.
 * Reemplaza a: CarruselMultimedia, Multimedia.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QPhotoCarousel(
    media: List<QMediaItem>,
    modifier: Modifier = Modifier,
    onVideoClick: (QMediaItem.Video) -> Unit = {},
) {
    QPlaceholder("QPhotoCarousel", modifier)
}
