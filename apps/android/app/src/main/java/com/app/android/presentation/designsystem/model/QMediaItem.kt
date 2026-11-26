// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.model

/**
 * Foto o video para QPhotoCarousel. Las páginas convierten su modelo a este.
 *
 * Dueño: P3 (Organismos).
 */
sealed class QMediaItem {
    data class Image(
        val url: String,
    ) : QMediaItem()

    data class Video(
        val videoUrl: String,
        val thumbnailUrl: String? = null,
    ) : QMediaItem()
}
