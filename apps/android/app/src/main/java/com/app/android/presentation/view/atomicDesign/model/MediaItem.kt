package com.app.android.presentation.view.atomicDesign.model

// Tipos de datos de multimedia que la molecula de multimedia puede manejar
sealed class MediaItem {
    data class Imagen(
        val url: String,
    ) : MediaItem()

    data class Video(
        val videoUrl: String,
        val thumbnailUrl: String? = null,
    ) : MediaItem()
}
