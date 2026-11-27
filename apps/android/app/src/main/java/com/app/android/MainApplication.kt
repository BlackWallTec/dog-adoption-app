package com.app.android

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class MainApplication :
    Application(),
    SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            // 1. Caché en Memoria RAM
            .memoryCache {
                MemoryCache
                    .Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            // 2. Caché en Disco Persistente
            .diskCache {
                DiskCache
                    .Builder()
                    .directory(cacheDir.resolve("dog_images_cache").toOkioPath())
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }.crossfade(true)
            .build()
}

// Nota mental: si cargas imágenes desde internet con Coil 3, asegúrate de tener agregada la dependencia de red correspondiente, como io.coil-kt.coil3:coil-network-okhttp
